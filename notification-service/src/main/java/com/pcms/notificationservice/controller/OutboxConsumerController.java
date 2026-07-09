package com.pcms.notificationservice.controller;

import com.pcms.notificationservice.dto.request.ExpiryAlertNotificationRequest;
import com.pcms.notificationservice.dto.request.LowStockNotificationRequest;
import com.pcms.notificationservice.dto.request.OrderPaidNotificationRequest;
import com.pcms.notificationservice.service.OutboxConsumerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * B6 + B7: Outbox event consumers.
 * Called by order-service (order.paid) and inventory-service (low-stock, expiry).
 *
 * <p>Idempotency is guaranteed by {@code X-Outbox-Event-Id}. Re-delivery of the
 * same event returns success without creating duplicate notifications.
 */
@RestController
@RequestMapping("/notifications")
public class OutboxConsumerController {

    private final OutboxConsumerService outboxConsumerService;

    public OutboxConsumerController(OutboxConsumerService outboxConsumerService) {
        this.outboxConsumerService = outboxConsumerService;
    }

    @PostMapping("/orders/paid")
    public ResponseEntity<Map<String, Object>> orderPaid(
            @RequestHeader(value = "X-Outbox-Event-Id", required = false) UUID eventId,
            @Valid @RequestBody OrderPaidNotificationRequest request) {
        return ResponseEntity.ok(outboxConsumerService.handleOrderPaid(eventId, request));
    }

    @PostMapping("/inventory/low-stock")
    public ResponseEntity<Map<String, Object>> lowStock(
            @RequestHeader(value = "X-Outbox-Event-Id", required = false) UUID eventId,
            @Valid @RequestBody LowStockNotificationRequest request) {
        return ResponseEntity.ok(outboxConsumerService.handleLowStock(eventId, request));
    }

    @PostMapping("/inventory/expiry")
    public ResponseEntity<Map<String, Object>> expiryAlert(
            @RequestHeader(value = "X-Outbox-Event-Id", required = false) UUID eventId,
            @Valid @RequestBody ExpiryAlertNotificationRequest request) {
        return ResponseEntity.ok(outboxConsumerService.handleExpiryAlert(eventId, request));
    }
}
