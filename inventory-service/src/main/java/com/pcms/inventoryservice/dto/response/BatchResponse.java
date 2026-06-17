package com.pcms.inventoryservice.dto.response;

import com.pcms.inventoryservice.entity.InventoryBatch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BatchResponse(
        UUID id,
        UUID medicineId,
        UUID branchId,
        String batchNo,
        String barcode,
        Integer qtyOnHand,
        LocalDate expiryDate,
        Integer minStockLevel,
        LocalDateTime receivedAt) {
    public static BatchResponse from(InventoryBatch b) {
        return new BatchResponse(
                b.getId(),
                b.getMedicineId(),
                b.getBranchId(),
                b.getBatchNo(),
                b.getBarcode(),
                b.getQtyOnHand(),
                b.getExpiryDate(),
                b.getMinStockLevel(),
                b.getReceivedAt());
    }
}
