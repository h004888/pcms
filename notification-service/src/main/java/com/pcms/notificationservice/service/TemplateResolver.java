package com.pcms.notificationservice.service;

import com.pcms.notificationservice.dto.response.ResolvedTemplateResponse;
import com.pcms.notificationservice.enums.NotificationChannel;

import java.util.Map;

public interface TemplateResolver {
    ResolvedTemplateResponse resolve(String templateCode, NotificationChannel channel,
            String fallbackTitle, String fallbackBody, Map<String, Object> variables);
}