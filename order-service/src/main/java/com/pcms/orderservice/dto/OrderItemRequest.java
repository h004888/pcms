package com.pcms.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for an item in a create-order request.
 */
public record OrderItemRequest(
    @NotNull UUID medicineId,
    @NotNull @Min(1) Integer quantity
) {}
