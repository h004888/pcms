package com.pcms.notificationservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ExpiryAlertNotificationRequest(
        @NotNull(message = "Mã chi nhánh là bắt buộc") UUID branchId,
        @NotNull(message = "Mã thuốc là bắt buộc") UUID medicineId,
        String medicineName,
        @NotBlank(message = "Mã lô là bắt buộc") String batchNo,
        @NotNull(message = "Ngày hết hạn là bắt buộc") LocalDate expiryDate,
        UUID recipientId) {
}