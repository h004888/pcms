package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InstallmentQuoteRequest(
        @NotNull(message = "amount is required")
        @Positive(message = "amount must be positive")
        BigDecimal amount,

        @NotNull(message = "months is required")
        @Positive(message = "months must be positive")
        Integer months,

        String provider
) {}
