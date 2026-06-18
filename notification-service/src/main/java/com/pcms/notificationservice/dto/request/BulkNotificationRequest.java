package com.pcms.notificationservice.dto.request;

import com.pcms.notificationservice.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record BulkNotificationRequest(
        @NotEmpty(message = "Danh sách người nhận không được để trống") List<UUID> recipientIds,
        @NotNull(message = "Kênh gửi là bắt buộc") NotificationChannel channel,
        String template,
        @NotBlank(message = "Tiêu đề không được để trống") String title,
        @NotBlank(message = "Nội dung không được để trống") String body) {
}