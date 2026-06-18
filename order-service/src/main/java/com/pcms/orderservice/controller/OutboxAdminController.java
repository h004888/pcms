package com.pcms.orderservice.controller;

import com.pcms.orderservice.service.OutboxAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/outbox")
public class OutboxAdminController {

    private final OutboxAdminService outboxAdminService;

    public OutboxAdminController(OutboxAdminService outboxAdminService) {
        this.outboxAdminService = outboxAdminService;
    }

    @PostMapping("/retry/{id}")
    public ResponseEntity<Map<String, Object>> retry(@PathVariable UUID id) {
        UUID newOutboxEventId = outboxAdminService.retryDeadLetter(id);
        return ResponseEntity.ok(Map.of(
                "message", "Outbox event requeued successfully",
                "outboxEventId", newOutboxEventId));
    }
}