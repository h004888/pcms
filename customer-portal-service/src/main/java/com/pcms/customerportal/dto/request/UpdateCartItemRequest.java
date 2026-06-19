package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemRequest(
        @NotNull(message = "qty is required")
        @Positive(message = "qty must be positive")
        Integer qty
) {}
