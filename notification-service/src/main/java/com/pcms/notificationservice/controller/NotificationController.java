package com.pcms.notificationservice.controller;

import com.pcms.notificationservice.entity.Notification;
import com.pcms.notificationservice.enums.NotificationStatus;
import com.pcms.notificationservice.repository.NotificationRepository;
import com.pcms.notificationservice.service.NotificationSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * UC13 - Notifications (SCR-NOTIF-LIST, SCR-NOTIF-COMPOSE)
 * FR13.1, FR13.2, FR13.3, FR13.4
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationSenderService senderService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam UUID recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Notification> notifications = notificationRepository.findByRecipientId(recipientId, pageable);
        return ResponseEntity.ok(Map.of(
            "data", notifications.getContent(),
            "page", notifications.getNumber(),
            "total", notifications.getTotalElements()
        ));
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> unread(
            @RequestParam UUID recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Notification> notifications = notificationRepository.findByRecipientIdAndStatus(
            recipientId, NotificationStatus.SENT, pageable);
        return ResponseEntity.ok(Map.of(
            "data", notifications.getContent(),
            "total", notifications.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getById(@PathVariable UUID id) {
        return notificationRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/notifications - Step 2-5 of UC13 main flow
     * Admin/CEO compose or system-triggered
     */
    @PostMapping
    public ResponseEntity<?> send(@RequestBody Notification notification) {
        notification.setStatus(NotificationStatus.PENDING);
        Notification saved = notificationRepository.save(notification);
        senderService.sendNotification(saved);
        return ResponseEntity.ok(saved);
    }

    /** POST /api/v1/notifications/bulk - Compose broadcast (AT1) */
    @PostMapping("/bulk")
    public ResponseEntity<?> sendBulk(@RequestBody BulkRequest request) {
        int sent = 0;
        for (UUID recipientId : request.recipientIds()) {
            Notification n = new Notification();
            n.setRecipientId(recipientId);
            n.setChannel(request.channel());
            n.setTitle(request.title());
            n.setBody(request.body());
            n.setTemplate(request.template());
            n.setStatus(NotificationStatus.PENDING);
            Notification saved = notificationRepository.save(n);
            senderService.sendNotification(saved);
            sent++;
        }
        return ResponseEntity.ok(Map.of("message", "Notifications queued", "count", sent));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable UUID id) {
        Optional<Notification> optional = notificationRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Notification n = optional.get();
        n.setStatus(NotificationStatus.READ);
        n.setReadAt(LocalDateTime.now());
        return ResponseEntity.ok(notificationRepository.save(n));
    }

    public record BulkRequest(List<UUID> recipientIds, com.pcms.notificationservice.enums.NotificationChannel channel,
                               String template, String title, String body) {}
}
