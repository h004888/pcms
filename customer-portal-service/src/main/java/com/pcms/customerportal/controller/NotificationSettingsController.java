package com.pcms.customerportal.controller;

import com.pcms.customerportal.dto.request.UpdateNotificationSettingsPatchRequest;
import com.pcms.customerportal.dto.request.UpdateNotificationSettingsRequest;
import com.pcms.customerportal.dto.response.NotificationSettingsResponse;
import com.pcms.customerportal.security.CurrentUser;
import com.pcms.customerportal.service.NotificationSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notif-settings")
@Tag(name = "UC14 - Customer Account / Notification Settings")
public class NotificationSettingsController {

    private final NotificationSettingsService service;

    public NotificationSettingsController(NotificationSettingsService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get my notification settings (auto-creates defaults)")
    public ResponseEntity<NotificationSettingsResponse> get(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId) {
        return ResponseEntity.ok(service.get(CurrentUser.requireCustomerId(userId)));
    }

    @PutMapping
    @Operation(summary = "Replace all settings (every flag required)")
    public ResponseEntity<NotificationSettingsResponse> update(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @Valid @RequestBody UpdateNotificationSettingsRequest request) {
        return ResponseEntity.ok(service.update(CurrentUser.requireCustomerId(userId), request));
    }

    @PatchMapping
    @Operation(summary = "Partial update (only non-null flags applied)")
    public ResponseEntity<NotificationSettingsResponse> patch(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @RequestBody UpdateNotificationSettingsPatchRequest request) {
        return ResponseEntity.ok(service.patch(CurrentUser.requireCustomerId(userId), request));
    }
}
