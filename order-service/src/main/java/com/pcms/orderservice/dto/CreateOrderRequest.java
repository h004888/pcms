package com.pcms.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a new order (UC06 - Step 1-12 of main flow).
 */
public record CreateOrderRequest(
                @NotNull UUID customerId,
                @NotNull UUID branchId,
                UUID staffId,
                UUID prescriptionId,
                @NotNull @Valid List<OrderItemRequest> items,
                String couponCode) {

        public CreateOrderRequest(UUID customerId,
                        UUID branchId,
                        UUID staffId,
                        List<OrderItemRequest> items,
                        String couponCode) {
                this(customerId, branchId, staffId, null, items, couponCode);
        }
}
