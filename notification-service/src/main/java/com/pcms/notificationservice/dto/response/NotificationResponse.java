package com.pcms.notificationservice.dto.response;

import com.pcms.notificationservice.enums.NotificationChannel;
import com.pcms.notificationservice.enums.NotificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID recipientId,
        NotificationChannel channel,
        String template,
        String title,
        String body,
        NotificationStatus status,
        LocalDateTime sentAt,
        LocalDateTime readAt,
        LocalDateTime createdAt) {
}