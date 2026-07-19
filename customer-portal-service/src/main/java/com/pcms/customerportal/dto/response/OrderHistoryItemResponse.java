package com.pcms.customerportal.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

public record OrderHistoryItemResponse(
        UUID id,
        String orderNumber,
        String status,
        BigDecimal total,
        int itemCount,
        Instant createdAt,
        List<ItemPreview> items
) {
    public record ItemPreview(UUID medicineId, String medicineName, int quantity) {}
}
