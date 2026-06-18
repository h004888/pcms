package com.pcms.inventoryservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "notification-service")
public interface NotificationClient {
    Logger log = LoggerFactory.getLogger(NotificationClient.class);

    @PostMapping("/notifications/inventory/low-stock")
    @CircuitBreaker(name = "notificationLowStockService", fallbackMethod = "fallbackLowStock")
    ResponseEntity<Object> lowStock(
            @RequestHeader("X-Outbox-Event-Id") String eventId,
            @RequestBody Map<String, Object> payload);

    @PostMapping("/notifications/inventory/expiry")
    @CircuitBreaker(name = "notificationExpiryService", fallbackMethod = "fallbackExpiry")
    ResponseEntity<Object> expiry(
            @RequestHeader("X-Outbox-Event-Id") String eventId,
            @RequestBody Map<String, Object> payload);

    default ResponseEntity<Object> fallbackLowStock(String eventId, Map<String, Object> payload, Throwable t) {
        log.warn("Low-stock notification fallback for event {}: {}", eventId, t.getMessage());
        return ResponseEntity.accepted().body(Map.of(
                "status", "DEFERRED",
                "eventId", eventId,
                "message", "Notification service unavailable, low-stock alert deferred"));
    }

    default ResponseEntity<Object> fallbackExpiry(String eventId, Map<String, Object> payload, Throwable t) {
        log.warn("Expiry notification fallback for event {}: {}", eventId, t.getMessage());
        return ResponseEntity.accepted().body(Map.of(
                "status", "DEFERRED",
                "eventId", eventId,
                "message", "Notification service unavailable, expiry alert deferred"));
    }
}