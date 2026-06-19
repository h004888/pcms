package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CheckoutPreviewRequest(
        @NotNull(message = "addressId is required")
        UUID addressId,

        @NotNull(message = "shippingMethod is required")
        String shippingMethod,

        String voucherCode
) {}
