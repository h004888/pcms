package com.pcms.notificationservice.dto.response;

import com.pcms.notificationservice.entity.NotificationTemplate;
import com.pcms.notificationservice.enums.NotificationChannel;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationTemplateResponse(
        UUID id,
        String code,
        NotificationChannel channel,
        String titleTemplate,
        String bodyTemplate,
        String variables,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static NotificationTemplateResponse from(NotificationTemplate template) {
        return new NotificationTemplateResponse(
                template.getId(),
                template.getCode(),
                template.getChannel(),
                template.getTitleTemplate(),
                template.getBodyTemplate(),
                template.getVariables(),
                template.getActive(),
                template.getCreatedAt(),
                template.getUpdatedAt());
    }
}