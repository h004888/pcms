package com.pcms.inventoryservice.controller;

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
}