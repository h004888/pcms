package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * SPRINT 3 - T11: POST /reviews request body.
 * customerId lấy từ X-Customer-Id header (gateway resolve), không nhận từ body.
 */
public record ReviewRequest(
        @NotNull UUID medicineId,
        @NotNull @Min(1) @Max(5) Integer rating,
        String comment
) {}