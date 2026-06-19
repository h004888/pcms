package com.pcms.pharmacistworkbench.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request to mark a customer as VIP.
 */
public record MarkVipRequest(
        @NotNull UUID customerId,
        @NotNull String tier,  // BRONZE, SILVER, GOLD, PLATINUM
        String reason
) {}
