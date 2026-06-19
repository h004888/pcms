package com.pcms.pharmacistworkbench.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record VipMarkResponse(
        UUID customerId,
        UUID markedBy,
        String tier,
        Integer loyaltyScore,
        BigDecimal lifetimeSpend,
        LocalDateTime markedAt,
        String reason
) {}
