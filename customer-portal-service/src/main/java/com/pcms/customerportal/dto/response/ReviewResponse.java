package com.pcms.customerportal.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SPRINT 3 - T11: Response DTO cho /reviews endpoints.
 */
public record ReviewResponse(
        UUID id,
        UUID customerId,
        UUID medicineId,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {}