package com.pcms.orderservice.dto;

import com.pcms.orderservice.entity.Order;
import com.pcms.orderservice.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for an Order.
 */
public record OrderResponse(
        UUID id,
        String orderNumber,
        UUID customerId,
        String customerName,
        UUID branchId,
        String branchName,
        UUID staffId,
        String staffName,
        UUID prescriptionId,
        String couponCode,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        OrderStatus status,
        List<OrderItemResponse> items,
        LocalDateTime createdAt) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getCustomerId(),
                null,
                o.getBranchId(),
                null,
                o.getStaffId(),
                null,
                o.getPrescriptionId(),
                o.getCouponCode(),
                o.getSubtotal(),
                o.getDiscount(),
                o.getTotal(),
                o.getStatus(),
                o.getItems() == null ? List.of() : o.getItems().stream().map(OrderItemResponse::from).toList(),
                o.getCreatedAt());
    }

    public static OrderResponse from(Order o, String customerName, String branchName, String staffName) {
        return new OrderResponse(
                o.getId(),
                o.getOrderNumber(),
                o.getCustomerId(),
                customerName,
                o.getBranchId(),
                branchName,
                o.getStaffId(),
                staffName,
                o.getPrescriptionId(),
                o.getCouponCode(),
                o.getSubtotal(),
                o.getDiscount(),
                o.getTotal(),
                o.getStatus(),
                o.getItems() == null ? List.of() : o.getItems().stream().map(OrderItemResponse::from).toList(),
                o.getCreatedAt());
    }
}
