package com.pcms.customerservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerOrderSummaryResponse(
        UUID id,
        String orderNumber,
        UUID customerId,
        UUID branchId,
        UUID staffId,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        String status,
        Object items,
        LocalDateTime createdAt) {
}