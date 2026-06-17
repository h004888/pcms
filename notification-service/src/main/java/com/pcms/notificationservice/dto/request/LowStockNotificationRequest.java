package com.pcms.notificationservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LowStockNotificationRequest(
        @NotNull(message = "Mã chi nhánh là bắt buộc") UUID branchId,
        @NotNull(message = "Mã thuốc là bắt buộc") UUID medicineId,
        String medicineName,
        @NotNull(message = "Số lượng tồn là bắt buộc") Integer qtyOnHand,
        @NotNull(message = "Ngưỡng tồn tối thiểu là bắt buộc") Integer minQty,
        UUID recipientId) {
}