package com.pcms.inventoryservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record BulkImportBatchItemRequest(
        @NotNull(message = "Mã thuốc không được để trống") UUID medicineId,

        @NotNull(message = "Mã chi nhánh không được để trống") UUID branchId,

        @NotBlank(message = "Số lô không được để trống") String batchNo,

        String barcode,

        @NotNull(message = "Số lượng không được để trống") Integer qty,

        @NotNull(message = "Ngày hết hạn không được để trống") LocalDate expiryDate,
        Integer minStockLevel,
        UUID supplierId,
        UUID actorId) {

    public BulkImportBatchItemRequest(UUID medicineId,
            UUID branchId,
            String batchNo,
            Integer qty,
            LocalDate expiryDate,
            Integer minStockLevel,
            UUID supplierId,
            UUID actorId) {
        this(medicineId, branchId, batchNo, null, qty, expiryDate, minStockLevel, supplierId, actorId);
    }
}