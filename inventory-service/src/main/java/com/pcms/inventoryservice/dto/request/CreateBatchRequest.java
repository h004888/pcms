package com.pcms.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateBatchRequest(
        @NotNull UUID medicineId,
        @NotNull UUID branchId,
        @NotBlank String batchNo,
        String barcode,
        @NotNull @Min(1) Integer qty,
        @NotNull LocalDate expiryDate,
        UUID actorId,
        UUID supplierId,
        /** B-07: Optional min stock level. Defaults to 10 if null. */
        Integer minStockLevel) {

    public CreateBatchRequest(UUID medicineId,
            UUID branchId,
            String batchNo,
            Integer qty,
            LocalDate expiryDate,
            UUID actorId,
            UUID supplierId,
            Integer minStockLevel) {
        this(medicineId, branchId, batchNo, null, qty, expiryDate, actorId, supplierId, minStockLevel);
    }
}
