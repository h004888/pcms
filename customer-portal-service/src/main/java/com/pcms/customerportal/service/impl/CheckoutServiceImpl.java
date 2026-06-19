package com.pcms.customerportal.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.client.OrderClient;
import com.pcms.customerportal.dto.request.CheckoutConfirmRequest;
import com.pcms.customerportal.dto.request.CheckoutPreviewRequest;
import com.pcms.customerportal.dto.response.CheckoutConfirmResponse;
import com.pcms.customerportal.dto.response.CheckoutPreviewResponse;
import com.pcms.customerportal.entity.Cart;
import com.pcms.customerportal.entity.CartItem;
import com.pcms.customerportal.enums.CartStatus;
import com.pcms.customerportal.repository.CartItemRepository;
import com.pcms.customerportal.repository.CartRepository;
import com.pcms.customerportal.service.CheckoutService;
import com.pcms.customerportal.service.VoucherService;
import com.pcms.customerportal.dto.request.ApplyVoucherRequest;
import com.pcms.customerportal.dto.response.ApplyVoucherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutServiceImpl.class);
    private static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("30000");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("200000");
    private static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.05");

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final VoucherService voucherService;
    private final OrderClient orderClient;

    public CheckoutServiceImpl(CartRepository cartRepository,
                               CartItemRepository cartItemRepository,
                               VoucherService voucherService,
                               OrderClient orderClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.voucherService = voucherService;
        this.orderClient = orderClient;
    }

    @Override
    @Transactional(readOnly = true)
    public CheckoutPreviewResponse preview(UUID customerId, CheckoutPreviewRequest request) {
        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "customer=" + customerId));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new InvalidOperationException("Cart is empty", "Giỏ hàng trống");
        }

        // Calculate subtotal + line discounts (BR04)
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal lineDiscountTotal = BigDecimal.ZERO;
        List<String> warnings = new ArrayList<>();

        for (CartItem item : items) {
            BigDecimal itemSubtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQty()));
            subtotal = subtotal.add(itemSubtotal);

            if (item.getQty() >= 10) {
                BigDecimal lineDiscount = itemSubtotal.multiply(BULK_DISCOUNT_RATE)
                        .setScale(2, RoundingMode.HALF_UP);
                lineDiscountTotal = lineDiscountTotal.add(lineDiscount);
                warnings.add(item.getMedicineName() + ": -5% bulk discount");
            }
        }

        // Apply voucher if provided
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        if (request.voucherCode() != null && !request.voucherCode().isBlank()) {
            ApplyVoucherResponse voucherResult = voucherService.apply(customerId,
                    new ApplyVoucherRequest(request.voucherCode()), subtotal);
            if (voucherResult.valid()) {
                voucherDiscount = voucherResult.discount();
            } else {
                warnings.add("Voucher: " + voucherResult.reason());
            }
        }

        BigDecimal totalDiscount = lineDiscountTotal.add(voucherDiscount);

        // Shipping fee
        BigDecimal shippingFee = DEFAULT_SHIPPING_FEE;
        if (subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            shippingFee = BigDecimal.ZERO;
            warnings.add("Free shipping for orders over " + FREE_SHIPPING_THRESHOLD + " VND");
        }

        // Total = subtotal - discount + shipping
        BigDecimal total = subtotal.subtract(totalDiscount).add(shippingFee);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        return new CheckoutPreviewResponse(subtotal, totalDiscount, shippingFee, total, warnings);
    }

    @Override
    @Transactional
    public CheckoutConfirmResponse confirm(UUID customerId, CheckoutConfirmRequest request) {
        // Get cart first
        Cart cart = cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "customer=" + customerId));

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new InvalidOperationException("Cart is empty", "Giỏ hàng trống");
        }

        // Create order via order-service
        List<Map<String, Object>> orderItems = items.stream()
                .map(item -> Map.<String, Object>of(
                        "medicineId", item.getMedicineId().toString(),
                        "qty", item.getQty(),
                        "unitPrice", item.getUnitPrice()))
                .toList();

        Map<String, Object> orderRequest = Map.of(
                "customerId", customerId.toString(),
                "addressId", request.addressId().toString(),
                "paymentMethod", request.paymentMethod(),
                "shippingMethod", request.shippingMethod(),
                "items", orderItems
        );

        Map<String, Object> orderResponse;
        try {
            orderResponse = orderClient.createOrder(orderRequest);
        } catch (Exception e) {
            log.error("[Checkout] Failed to create order for customer={}: {}", customerId, e.getMessage());
            throw new InvalidOperationException("Order creation failed", "Không thể tạo đơn hàng");
        }

        UUID orderId = UUID.fromString((String) orderResponse.getOrDefault("id", ""));
        String orderNumber = (String) orderResponse.getOrDefault("orderNumber", "");
        String orderStatus = (String) orderResponse.getOrDefault("status", "PENDING");
        BigDecimal total = cart.getTotal();

        // Mark cart as checked out
        cart.setStatus(CartStatus.CHECKED_OUT);
        cartRepository.save(cart);

        log.info("[Checkout] confirmed order={} for customer={}", orderNumber, customerId);

        return new CheckoutConfirmResponse(orderId, orderNumber, null, total, orderStatus);
    }
}
