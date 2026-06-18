package com.pcms.notificationservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderPaidNotificationRequest(
        @NotNull(message = "Mã đơn hàng là bắt buộc") UUID orderId,
        @NotNull(message = "Số đơn hàng là bắt buộc") String orderNumber,
        @NotNull(message = "Mã khách hàng là bắt buộc") UUID customerId,
        UUID branchId,
        UUID staffId,
        @NotNull(message = "Tổng tiền là bắt buộc") BigDecimal total,
        LocalDateTime paidAt) {
}