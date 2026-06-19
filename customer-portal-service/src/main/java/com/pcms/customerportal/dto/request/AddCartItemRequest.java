package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddCartItemRequest(
        @NotNull(message = "medicineId is required")
        UUID medicineId,

        @NotNull(message = "qty is required")
        @Positive(message = "qty must be positive")
        Integer qty
) {}
