package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApplyVoucherRequest(
        @NotBlank(message = "voucherCode is required")
        String voucherCode
) {}
