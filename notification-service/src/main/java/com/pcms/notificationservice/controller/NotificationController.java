package com.pcms.notificationservice.controller;

import com.pcms.common.dto.PageResponse;
import com.pcms.notificationservice.dto.request.BroadcastRequest;
import com.pcms.notificationservice.dto.request.BulkNotificationRequest;
import com.pcms.notificationservice.dto.request.ComposeNotificationRequest;
import com.pcms.notificationservice.dto.request.CreateNotificationRequest;
import com.pcms.notificationservice.dto.response.NotificationResponse;
import com.pcms.notificationservice.enums.NotificationStatus;
import com.pcms.notificationservice.repository.NotificationRepository;
import com.pcms.notificationservice.service.NotificationSenderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC13 - Notifications (SCR-NOTIF-LIST, SCR-NOTIF-COMPOSE)
 * FR13.1, FR13.2, FR13.3, FR13.4
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationSenderService senderService;
    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationSenderService senderService,
                                  NotificationRepository notificationRepository) {
        this.senderService = senderService;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> list(
            @RequestParam UUID recipientId,
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        NotificationStatus resolvedStatus = resolveStatus(status);
        return ResponseEntity.ok(senderService.list(recipientId, resolvedStatus, page, size));
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> unread(
            @RequestParam UUID recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(Map.of("data", senderService.list(recipientId, NotificationStatus.SENT, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(senderService.getById(id));
    }

    /**
     * POST /api/v1/notifications - Step 2-5 of UC13 main flow
     * Admin/CEO compose or system-triggered
     */
    @PostMapping
    public ResponseEntity<NotificationResponse> send(@Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.ok(senderService.createAndSend(request));
    }

    /** POST /api/v1/notifications/bulk - Compose broadcast (AT1) */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> sendBulk(@Valid @RequestBody BulkNotificationRequest request) {
        int sent = 0;
        for (UUID recipientId : request.recipientIds()) {
            CreateNotificationRequest single = new CreateNotificationRequest(
                    recipientId,
                    request.channel(),
                    request.template(),
                    request.title(),
                    request.body(),
                    null);
            senderService.createAndSend(single);
            sent++;
        }
        return ResponseEntity.ok(Map.of("message", "Notifications queued", "count", sent));
    }

    /** POST /api/v1/notifications/broadcast - role/branch-based broadcast */
    @PostMapping("/broadcast")
    public ResponseEntity<Map<String, Object>> broadcast(@Valid @RequestBody BroadcastRequest request) {
        int count = senderService.broadcast(request);
        return ResponseEntity.ok(Map.of("message", "Broadcast queued", "count", count));
    }

    /** POST /api/v1/notifications/compose - template-based compose screen */
    @PostMapping("/compose")
    public ResponseEntity<Map<String, Object>> compose(@Valid @RequestBody ComposeNotificationRequest request) {
        int count = senderService.compose(request);
        return ResponseEntity.ok(Map.of("message", "Notifications queued", "count", count));
    }

    /** POST /api/v1/notifications/{id}/retry - NSF-09 manual retry */
    @PostMapping("/{id}/retry")
    public ResponseEntity<NotificationResponse> retry(@PathVariable UUID id) {
        return ResponseEntity.ok(senderService.retry(id));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable UUID id) {
        return ResponseEntity.ok(senderService.markAsRead(id));
    }

    /** PUT /notifications/read-all — mark all of a recipient's unread as read. */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead(@RequestParam UUID recipientId) {
        int updated = senderService.markAllAsRead(recipientId);
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    /**
     * TICKET-304: DELETE /notifications/{id} - soft-delete a notification.
     * Only the recipient can delete their own notification. Anonymous
     * deletes are rejected with 403.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<NotificationResponse> delete(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        if (userId == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "X-User-Id header is required to delete a notification");
        }
        return ResponseEntity.ok(senderService.softDelete(id, userId));
    }

    private NotificationStatus resolveStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return null;
        }
        if ("unread".equalsIgnoreCase(status)) {
            return NotificationStatus.SENT;
        }
        if ("read".equalsIgnoreCase(status)) {
            return NotificationStatus.READ;
        }
        return NotificationStatus.valueOf(status.toUpperCase());
    }

}
