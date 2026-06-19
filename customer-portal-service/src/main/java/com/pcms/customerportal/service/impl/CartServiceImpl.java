package com.pcms.customerportal.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.client.CatalogClient;
import com.pcms.customerportal.dto.request.AddCartItemRequest;
import com.pcms.customerportal.dto.request.UpdateCartItemRequest;
import com.pcms.customerportal.dto.response.CartItemResponse;
import com.pcms.customerportal.dto.response.CartResponse;
import com.pcms.customerportal.entity.Cart;
import com.pcms.customerportal.entity.CartItem;
import com.pcms.customerportal.enums.CartStatus;
import com.pcms.customerportal.repository.CartItemRepository;
import com.pcms.customerportal.repository.CartRepository;
import com.pcms.customerportal.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);
    private static final BigDecimal BULK_DISCOUNT_THRESHOLD = BigDecimal.TEN;
    private static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.05");
    private static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("30000");

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CatalogClient catalogClient;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           CatalogClient catalogClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.catalogClient = catalogClient;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID customerId) {
        Cart cart = getOrCreateCart(customerId);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(UUID customerId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(customerId);

        // Fetch medicine info from catalog-service
        Map<String, Object> medicine = catalogClient.getById(request.medicineId().toString());
        if (medicine == null || medicine.isEmpty()) {
            throw new ResourceNotFoundException("Medicine", request.medicineId());
        }

        String medicineName = (String) medicine.getOrDefault("name", "Unknown");
        String imageUrl = (String) medicine.get("imageUrl");
        BigDecimal unitPrice = extractPrice(medicine);

        // Check if same medicine already in cart
        Optional<CartItem> existing = cartItemRepository.findByCartIdAndMedicineId(
                cart.getId(), request.medicineId());

        CartItem item;
        if (existing.isPresent()) {
            item = existing.get();
            item.setQty(item.getQty() + request.qty());
            item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQty())));
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setMedicineId(request.medicineId());
            item.setMedicineName(medicineName);
            item.setImageUrl(imageUrl);
            item.setQty(request.qty());
            item.setUnitPrice(unitPrice);
            item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(request.qty())));
        }
        cartItemRepository.save(item);

        recalculateTotals(cart);
        cartRepository.save(cart);

        log.info("[Cart] added medicine={} qty={} to cart for customer={}", request.medicineId(), request.qty(), customerId);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(UUID customerId, UUID itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(customerId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new InvalidOperationException("Item does not belong to this cart", "Sản phẩm không thuộc giỏ hàng của bạn");
        }

        item.setQty(request.qty());
        item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(request.qty())));
        cartItemRepository.save(item);

        recalculateTotals(cart);
        cartRepository.save(cart);

        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(UUID customerId, UUID itemId) {
        Cart cart = getOrCreateCart(customerId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new InvalidOperationException("Item does not belong to this cart", "Sản phẩm không thuộc giỏ hàng của bạn");
        }

        cart.removeItem(item);
        cartItemRepository.delete(item);

        recalculateTotals(cart);
        cartRepository.save(cart);

        log.info("[Cart] removed item={} from cart for customer={}", itemId, customerId);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clearCart(UUID customerId) {
        Cart cart = getOrCreateCart(customerId);
        cartItemRepository.deleteByCartId(cart.getId());
        cart.getItems().clear();
        cart.setSubtotal(BigDecimal.ZERO);
        cart.setDiscount(BigDecimal.ZERO);
        cart.setTotal(BigDecimal.ZERO);
        cart.setVoucherCode(null);
        cartRepository.save(cart);

        log.info("[Cart] cleared cart for customer={}", customerId);
        return toResponse(cart);
    }

    // ===== Private helpers =====

    private Cart getOrCreateCart(UUID customerId) {
        return cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomerId(customerId);
                    newCart.setStatus(CartStatus.ACTIVE);
                    newCart.setShippingFee(DEFAULT_SHIPPING_FEE);
                    return cartRepository.save(newCart);
                });
    }

    private void recalculateTotals(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal lineDiscountTotal = BigDecimal.ZERO;

        for (CartItem item : items) {
            item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQty())));
            subtotal = subtotal.add(item.getSubtotal());

            // BR04: 5% discount when qty >= 10 of same medicine
            if (item.getQty() >= 10) {
                BigDecimal lineDiscount = item.getSubtotal().multiply(BULK_DISCOUNT_RATE)
                        .setScale(2, RoundingMode.HALF_UP);
                lineDiscountTotal = lineDiscountTotal.add(lineDiscount);
            }
        }

        cart.setSubtotal(subtotal);
        cart.setDiscount(lineDiscountTotal);

        BigDecimal total = subtotal.subtract(lineDiscountTotal);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
        cart.setTotal(total);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        List<CartItemResponse> itemResponses = new ArrayList<>();
        for (CartItem item : items) {
            BigDecimal discount = BigDecimal.ZERO;
            if (item.getQty() >= 10) {
                discount = item.getSubtotal().multiply(BULK_DISCOUNT_RATE)
                        .setScale(2, RoundingMode.HALF_UP);
            }
            itemResponses.add(CartItemResponse.from(item, discount));
        }
        return CartResponse.from(cart, itemResponses);
    }

    @SuppressWarnings("unchecked")
    private BigDecimal extractPrice(Map<String, Object> medicine) {
        Object price = medicine.get("price");
        if (price instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        if (price instanceof String s) {
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                log.warn("Invalid price format: {}", price);
            }
        }
        return BigDecimal.ZERO;
    }
}
