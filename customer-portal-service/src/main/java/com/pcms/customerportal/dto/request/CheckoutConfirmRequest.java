package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CheckoutConfirmRequest(
        @NotNull(message = "addressId is required")
        UUID addressId,

        @NotBlank(message = "shippingMethod is required")
        String shippingMethod,

        String voucherCode,

        @NotBlank(message = "paymentMethod is required")
        String paymentMethod
) {}
