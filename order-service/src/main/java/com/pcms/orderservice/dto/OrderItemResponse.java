package com.pcms.orderservice.dto;

import com.pcms.orderservice.entity.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for an OrderItem.
 */
public record OrderItemResponse(
    UUID id,
    UUID medicineId,
    String medicineName,
    UUID batchId,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal discount,
    BigDecimal subtotal
) {
    public static OrderItemResponse from(OrderItem i) {
        return new OrderItemResponse(
            i.getId(),
            i.getMedicineId(),
            i.getMedicineName(),
            i.getBatchId(),
            i.getQty(),
            i.getUnitPrice(),
            i.getDiscount(),
            i.getSubtotal()
        );
    }
}
