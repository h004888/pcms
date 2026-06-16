package com.pcms.notificationservice.dto;

import com.pcms.notificationservice.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateNotificationRequest(
        @NotNull(message = "recipientId is required")
        UUID recipientId,
        @NotNull(message = "channel is required")
        NotificationChannel channel,
        String template,
        @NotBlank(message = "title is required")
        String title,
        @NotBlank(message = "body is required")
        String body,
        LocalDateTime sendAt
) {
}
