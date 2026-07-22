package com.pcms.customerportal.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.client.OrderClient;
import com.pcms.customerportal.client.PaymentServiceClient;
import com.pcms.customerportal.dto.request.CheckoutConfirmRequest;
import com.pcms.customerportal.dto.request.CheckoutPreviewRequest;
import com.pcms.customerportal.dto.response.CheckoutConfirmResponse;
import com.pcms.customerportal.dto.response.CheckoutPreviewResponse;
import com.pcms.customerportal.entity.Cart;
import com.pcms.customerportal.entity.CartItem;
import com.pcms.customerportal.enums.CartStatus;
import com.pcms.customerportal.repository.CartItemRepository;
import com.pcms.customerportal.repository.CartRepository;
import com.pcms.customerportal.service.CartFactory;
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
    // [TEMP-2026-07-19] Disabled: pickup-only phase, no delivery shipping fee
    // private static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("30000");
    // private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("200000");
    private static final BigDecimal BULK_DISCOUNT_RATE = new BigDecimal("0.05");

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final VoucherService voucherService;
    private final OrderClient orderClient;
    private final PaymentServiceClient paymentServiceClient;
    private final CartFactory cartFactory;

    public CheckoutServiceImpl(CartRepository cartRepository,
                               CartItemRepository cartItemRepository,
                               VoucherService voucherService,
                               OrderClient orderClient,
                               PaymentServiceClient paymentServiceClient,
                               CartFactory cartFactory) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.voucherService = voucherService;
        this.orderClient = orderClient;
        this.paymentServiceClient = paymentServiceClient;
        this.cartFactory = cartFactory;
    }

    @Override
    @Transactional(readOnly = true)
    public CheckoutPreviewResponse preview(UUID customerId, CheckoutPreviewRequest request) {
        Cart cart = cartFactory.getOrCreateCart(customerId);

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

        // [TEMP-2026-07-19] All orders are pickup → shipping always free
        BigDecimal shippingFee = BigDecimal.ZERO;

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
        Cart cart = cartFactory.getOrCreateCart(customerId);

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new InvalidOperationException("Cart is empty", "Giỏ hàng trống");
        }

        // Create order via order-service
        List<Map<String, Object>> orderItems = buildOrderItems(items);

        Map<String, Object> orderRequest = Map.of(
                "customerId", customerId.toString(),
                "branchId", request.branchId().toString(),
                "items", orderItems
        );

        log.info("[Checkout] Sending orderRequest to order-service: customerId={}, branchId={}, items={}",
                customerId, request.branchId(), orderItems.size());

        Map<String, Object> orderResponse;
        try {
            orderResponse = orderClient.createOrder(orderRequest);
            log.info("[Checkout] order-service responded: {}", orderResponse);
        } catch (Exception e) {
            log.error("[Checkout] FAILED to create order for customer={}: {} (type={})",
                    customerId, e.getMessage(), e.getClass().getSimpleName());
            if (e.getCause() != null) {
                log.error("[Checkout] Caused by: {} - {}", e.getCause().getClass().getSimpleName(), e.getCause().getMessage());
            }
            throw new InvalidOperationException("Order creation failed", "Không thể tạo đơn hàng");
        }

        String orderIdStr = (String) orderResponse.getOrDefault("id", null);
        if (orderIdStr == null || orderIdStr.isBlank()) {
            throw new InvalidOperationException("Order creation failed", "Không thể tạo đơn hàng");
        }
        UUID orderId = UUID.fromString(orderIdStr);
        String orderNumber = (String) orderResponse.getOrDefault("orderNumber", "");
        String orderStatus = (String) orderResponse.getOrDefault("status", "PENDING");
        BigDecimal total = parseTotal(orderResponse, cart.getTotal());

        // Create pending payment for VietQR
        if ("VIETQR".equalsIgnoreCase(request.paymentMethod())) {
            try {
                Map<String, Object> paymentRequest = Map.of(
                        "orderId", orderId.toString(),
                        "paymentMethod", "QR",
                        "amount", total,
                        "transactionRef", orderNumber
                );
                paymentServiceClient.createPayment(paymentRequest);
                log.info("[Checkout] created pending payment for order={} (VietQR)", orderNumber);
            } catch (Exception e) {
                log.warn("[Checkout] failed to create pending payment for order={}: {}",
                        orderNumber, e.getMessage());
            }
            // Don't mark cart as CHECKED_OUT yet — keep ACTIVE until payment confirmed
        } else {
            // COD: mark cart as checked out immediately
            cart.setStatus(CartStatus.CHECKED_OUT);
            cartRepository.save(cart);
        }

        log.info("[Checkout] confirmed order={} for customer={}", orderNumber, customerId);

        return new CheckoutConfirmResponse(orderId, orderNumber, null, total, orderStatus);
    }

    private List<Map<String, Object>> buildOrderItems(List<CartItem> items) {
        return items.stream()
                .map(item -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("medicineId", item.getMedicineId().toString());
                    m.put("quantity", item.getQty());
                    m.put("unitPrice", item.getUnitPrice());
                    return m;
                })
                .toList();
    }

    private BigDecimal parseTotal(Map<String, Object> orderResponse, BigDecimal fallback) {
        Object totalObj = orderResponse.get("total");
        if (totalObj instanceof BigDecimal bd) {
            return bd;
        }
        if (totalObj instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return fallback;
    }
}
