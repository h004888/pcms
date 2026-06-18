package com.pcms.supplierservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierHistoryResponse(
        UUID supplierId,
        String action,
        String description,
        LocalDateTime occurredAt) {
}