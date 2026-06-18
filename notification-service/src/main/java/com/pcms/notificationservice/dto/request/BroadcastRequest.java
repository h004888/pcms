package com.pcms.notificationservice.dto.request;

import com.pcms.notificationservice.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record BroadcastRequest(
        Audience audience,
        @NotEmpty(message = "Phải chọn ít nhất một kênh gửi") @NotNull(message = "Danh sách kênh gửi là bắt buộc") Set<NotificationChannel> channels,
        String template,
        @NotBlank(message = "Tiêu đề không được để trống") String title,
        @NotBlank(message = "Nội dung không được để trống") String body) {

    public record Audience(
            Set<String> roles,
            Set<UUID> users,
            Set<UUID> branches) {
    }

    public List<UUID> resolveRecipients() {
        if (audience == null) {
            return List.of();
        }
        if (audience.users() != null && !audience.users().isEmpty()) {
            return audience.users().stream().toList();
        }
        return List.of();
    }
}