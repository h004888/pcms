package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.CartItem;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID medicineId,
        String medicineName,
        String imageUrl,
        Integer qty,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        BigDecimal discount
) {
    public static CartItemResponse from(CartItem item, BigDecimal discount) {
        return new CartItemResponse(
                item.getId(),
                item.getMedicineId(),
                item.getMedicineName(),
                item.getImageUrl(),
                item.getQty(),
                item.getUnitPrice(),
                item.getSubtotal(),
                discount != null ? discount : BigDecimal.ZERO
        );
    }

    public static CartItemResponse from(CartItem item) {
        return from(item, BigDecimal.ZERO);
    }
}
