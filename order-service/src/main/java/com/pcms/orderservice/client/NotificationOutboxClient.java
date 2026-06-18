package com.pcms.orderservice.client;

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
public interface NotificationOutboxClient {

    Logger log = LoggerFactory.getLogger(NotificationOutboxClient.class);

    @PostMapping("/notifications/orders/paid")
    @CircuitBreaker(name = "notificationOutboxService", fallbackMethod = "fallbackOrderPaid")
    ResponseEntity<Object> orderPaid(@RequestHeader("X-Outbox-Event-Id") String eventId,
            @RequestBody Map<String, Object> payload);

    default ResponseEntity<Object> fallbackOrderPaid(String eventId, Map<String, Object> payload, Throwable t) {
        log.warn("Notification outbox fallback for event {}: {}", eventId, t.getMessage());
        return ResponseEntity.accepted().body(Map.of(
                "status", "DEFERRED",
                "eventId", eventId,
                "message", "Notification service unavailable, notification deferred"));
    }
}