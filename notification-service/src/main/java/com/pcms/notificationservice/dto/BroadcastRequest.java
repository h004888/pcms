package com.pcms.notificationservice.dto;

import com.pcms.notificationservice.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record BroadcastRequest(
        Audience audience,
        @NotEmpty(message = "channels are required")
        @NotNull
        Set<NotificationChannel> channels,
        String template,
        @NotBlank(message = "title is required")
        String title,
        @NotBlank(message = "body is required")
        String body
) {
    public record Audience(
            Set<String> roles,
            Set<UUID> users,
            Set<UUID> branches
    ) {
    }

    public List<UUID> resolveRecipients() {
        if (audience == null) return List.of();
        if (audience.users() != null && !audience.users().isEmpty()) {
            return audience.users().stream().toList();
        }
        // role/branch-based resolution is handled by the service layer
        return List.of();
    }
}
