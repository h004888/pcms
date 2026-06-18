package com.pcms.notificationservice.dto.request;

import com.pcms.notificationservice.enums.NotificationChannel;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record ComposeNotificationRequest(
                Set<UUID> recipientIds,
                UUID recipientId,
                Set<NotificationChannel> channels,
                NotificationChannel channel,
                String template,
                Map<String, Object> variables,
                String title,
                String body) {
}