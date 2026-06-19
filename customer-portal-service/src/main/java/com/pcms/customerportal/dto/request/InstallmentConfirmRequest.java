package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InstallmentConfirmRequest(
        @NotNull(message = "orderId is required")
        UUID orderId,

        @NotBlank(message = "provider is required")
        String provider,

        @NotBlank(message = "planId is required")
        String planId
) {}
