package com.pcms.notificationservice.controller;

import com.pcms.notificationservice.dto.request.CreateNotificationTemplateRequest;
import com.pcms.notificationservice.dto.response.NotificationTemplateResponse;
import com.pcms.notificationservice.enums.NotificationChannel;
import com.pcms.notificationservice.service.NotificationTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications/templates")
public class NotificationTemplateController {
    private final NotificationTemplateService templateService;

    public NotificationTemplateController(NotificationTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationTemplateResponse>> list(
            @RequestParam(required = false, defaultValue = "true") Boolean active,
            @RequestParam(required = false) NotificationChannel channel) {
        return ResponseEntity.ok(templateService.list(active, channel));
    }

    @PostMapping
    public ResponseEntity<NotificationTemplateResponse> create(
            @Valid @RequestBody CreateNotificationTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.create(request));
    }
}