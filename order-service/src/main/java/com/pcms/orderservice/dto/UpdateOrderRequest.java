package com.pcms.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO for updating a PENDING_PAYMENT order (replace items).
 */
public record UpdateOrderRequest(
    @NotNull @Valid List<OrderItemRequest> items
) {}
