package com.pcms.customerportal.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderHistoryItemResponse(
        UUID id,
        String orderNumber,
        String status,
        BigDecimal total,
        int itemCount,
        LocalDateTime createdAt
) {}
