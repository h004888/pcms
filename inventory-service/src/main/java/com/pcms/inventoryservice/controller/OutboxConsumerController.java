package com.pcms.inventoryservice.controller;

import com.pcms.inventoryservice.dto.BulkConsumeRequest;
import com.pcms.inventoryservice.dto.BulkRestoreRequest;
import com.pcms.inventoryservice.dto.request.ConsumeBatchRequest;
import com.pcms.inventoryservice.service.OutboxConsumerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Order outbox consumers for inventory stock side effects.
 *
 * <p>
 * These endpoints match duc.html while keeping the legacy /inventory/consume
 * endpoint intact for backward compatibility.
 */
@RestController
@RequestMapping("/inventory/orders")
public class OutboxConsumerController {

    private final OutboxConsumerService outboxConsumerService;

    public OutboxConsumerController(OutboxConsumerService outboxConsumerService) {
        this.outboxConsumerService = outboxConsumerService;
    }

    @PostMapping("/{orderId}/paid")
    public ResponseEntity<?> orderPaid(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Outbox-Event-Id", required = false) UUID eventId,
            @Valid @RequestBody ConsumeBatchRequest request) {
        return ResponseEntity.ok(outboxConsumerService.handleOrderPaid(orderId, eventId, request));
    }

    @PostMapping("/{orderId}/cancelled")
    public ResponseEntity<?> orderCancelled(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Outbox-Event-Id", required = false) UUID eventId,
            @Valid @RequestBody ConsumeBatchRequest request) {
        return ResponseEntity.ok(outboxConsumerService.handleOrderCancelled(orderId, eventId, request));
    }

    @PostMapping("/{orderId}/paid-bulk")
    public ResponseEntity<?> orderPaidBulk(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Outbox-Event-Id", required = false) UUID eventId,
            @Valid @RequestBody BulkConsumeRequest request) {
        return ResponseEntity.ok(outboxConsumerService.handleOrderPaidBulk(orderId, eventId, request));
    }

    @PostMapping("/{orderId}/cancelled-bulk")
    public ResponseEntity<?> orderCancelledBulk(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Outbox-Event-Id", required = false) UUID eventId,
            @Valid @RequestBody BulkRestoreRequest request) {
        return ResponseEntity.ok(outboxConsumerService.handleOrderCancelledBulk(orderId, eventId, request));
    }

    @PostMapping("/{orderId}/cancelled-precise")
    public ResponseEntity<?> orderCancelledPrecise(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Outbox-Event-Id", required = false) UUID eventId) {
        return ResponseEntity.ok(outboxConsumerService.handleOrderCancelledPrecise(orderId, eventId));
    }
}