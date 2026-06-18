package com.pcms.notificationservice.dto.response;

public record ResolvedTemplateResponse(
        String templateCode,
        String title,
        String body) {
}