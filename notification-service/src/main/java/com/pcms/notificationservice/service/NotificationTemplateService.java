package com.pcms.notificationservice.service;

import com.pcms.notificationservice.dto.request.CreateNotificationTemplateRequest;
import com.pcms.notificationservice.dto.response.NotificationTemplateResponse;
import com.pcms.notificationservice.enums.NotificationChannel;

import java.util.List;

public interface NotificationTemplateService {
    List<NotificationTemplateResponse> list(Boolean active, NotificationChannel channel);

    NotificationTemplateResponse create(CreateNotificationTemplateRequest request);
}