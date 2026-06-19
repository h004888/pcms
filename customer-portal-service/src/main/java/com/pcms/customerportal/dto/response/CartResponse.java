package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.Cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        UUID customerId,
        List<CartItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        String voucherCode,
        String status,
        LocalDateTime updatedAt
) {
    public static CartResponse from(Cart cart, List<CartItemResponse> items) {
        return new CartResponse(
                cart.getId(),
                cart.getCustomerId(),
                items,
                cart.getSubtotal(),
                cart.getDiscount(),
                cart.getTotal(),
                cart.getVoucherCode(),
                cart.getStatus().name(),
                cart.getUpdatedAt()
        );
    }
}
