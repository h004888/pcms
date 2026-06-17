package com.pcms.notificationservice.controller;

import com.pcms.notificationservice.dto.BroadcastRequest;
import com.pcms.notificationservice.dto.CreateNotificationRequest;
import com.pcms.notificationservice.dto.NotificationResponse;
import com.pcms.common.dto.PageResponse;
import com.pcms.notificationservice.entity.NotificationTemplate;
import com.pcms.notificationservice.enums.NotificationStatus;
import com.pcms.notificationservice.repository.NotificationRepository;
import com.pcms.notificationservice.repository.NotificationTemplateRepository;
import com.pcms.notificationservice.service.NotificationSenderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    private final NotificationTemplateRepository templateRepository;

    public NotificationController(NotificationSenderService senderService,
                                  NotificationRepository notificationRepository,
                                  NotificationTemplateRepository templateRepository) {
        this.senderService = senderService;
        this.notificationRepository = notificationRepository;
        this.templateRepository = templateRepository;
    }

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> list(
            @RequestParam UUID recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(senderService.list(recipientId, page, size));
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> unread(
            @RequestParam UUID recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<com.pcms.notificationservice.entity.Notification> notifications =
                notificationRepository.findByRecipientIdAndStatus(
                        recipientId, NotificationStatus.SENT, pageable);
        return ResponseEntity.ok(Map.of(
                "data", notifications.getContent(),
                "page", notifications.getNumber(),
                "total", notifications.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getById(@PathVariable UUID id) {
        // Reuse list-by-id via markAsRead? No - we want a GET. Provide a direct
        // repository lookup for backwards-compat with the previous endpoint.
        return notificationRepository.findById(id)
                .map(n -> ResponseEntity.ok(new NotificationResponse(
                        n.getId(), n.getRecipientId(), n.getChannel(), n.getTemplate(),
                        n.getTitle(), n.getBody(), n.getStatus(), n.getSentAt(), n.getReadAt(), n.getCreatedAt())))
                .orElseGet(() -> ResponseEntity.notFound().build());
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
    public ResponseEntity<Map<String, Object>> sendBulk(@Valid @RequestBody BulkRequest request) {
        int sent = 0;
        for (UUID recipientId : request.recipientIds()) {
            CreateNotificationRequest single = new CreateNotificationRequest(
                    recipientId,
                    request.channel(),
                    request.template(),
                    request.title(),
                    request.body(),
                    null
            );
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
        Pageable all = PageRequest.of(0, 1000);
        Page<com.pcms.notificationservice.entity.Notification> unread =
                notificationRepository.findByRecipientIdAndStatus(recipientId, NotificationStatus.SENT, all);
        int count = 0;
        for (com.pcms.notificationservice.entity.Notification n : unread.getContent()) {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
            count++;
        }
        return ResponseEntity.ok(Map.of("marked", count));
    }

    /** GET /notifications/templates — list all templates (B8). */
    @GetMapping("/templates")
    public ResponseEntity<List<NotificationTemplate>> listTemplates() {
        return ResponseEntity.ok(templateRepository.findAll());
    }

    /** POST /notifications/templates — create template (B8). */
    @PostMapping("/templates")
    public ResponseEntity<NotificationTemplate> createTemplate(
            @RequestBody NotificationTemplate template) {
        return ResponseEntity.ok(templateRepository.save(template));
    }

    public record BulkRequest(List<UUID> recipientIds,
                              com.pcms.notificationservice.enums.NotificationChannel channel,
                              String template,
                              String title,
                              String body) {
    }
}
