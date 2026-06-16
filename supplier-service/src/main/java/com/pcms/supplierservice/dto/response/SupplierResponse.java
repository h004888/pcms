package com.pcms.supplierservice.dto.response;

import com.pcms.supplierservice.enums.SupplierStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String name,
        String taxCode,
        String contactPerson,
        String phone,
        String email,
        String address,
        String bankName,
        String bankAccount,
        SupplierStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
