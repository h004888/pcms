package com.pcms.customerportal.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID id,
        String orderNumber,
        String status,
        UUID branchId,
        UUID prescriptionId,
        String couponCode,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        Instant createdAt,
        List<Item> items
) {
    public record Item(
            UUID medicineId,
            String medicineName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal discount,
            BigDecimal subtotal
    ) {}
}
