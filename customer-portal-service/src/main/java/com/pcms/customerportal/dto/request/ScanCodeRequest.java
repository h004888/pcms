package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request for SHOP-VERIFY-ORIGIN QR scan.
 */
public record ScanCodeRequest(
        @NotBlank String code
) {}