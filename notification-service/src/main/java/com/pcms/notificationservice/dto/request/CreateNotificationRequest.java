package com.pcms.notificationservice.dto.request;

import com.pcms.notificationservice.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateNotificationRequest(
        @NotNull(message = "Mã người nhận là bắt buộc") UUID recipientId,
        @NotNull(message = "Kênh gửi là bắt buộc") NotificationChannel channel,
        String template,
        @NotBlank(message = "Tiêu đề không được để trống") String title,
        @NotBlank(message = "Nội dung không được để trống") String body,
        LocalDateTime sendAt) {
}