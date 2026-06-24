package com.pcms.customerportal.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderHistoryItemResponse(
        UUID id,
        String orderNumber,
        String status,
        BigDecimal total,
        int itemCount,
        Instant createdAt
) {}
