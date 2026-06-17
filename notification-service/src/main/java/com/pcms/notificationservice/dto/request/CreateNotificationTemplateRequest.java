package com.pcms.notificationservice.dto.request;

import com.pcms.notificationservice.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNotificationTemplateRequest(
        @NotBlank(message = "Mã template không được để trống") String code,
        @NotNull(message = "Kênh gửi là bắt buộc") NotificationChannel channel,
        @NotBlank(message = "Tiêu đề template không được để trống") String titleTemplate,
        @NotBlank(message = "Nội dung template không được để trống") String bodyTemplate,
        String variables,
        Boolean active) {
}