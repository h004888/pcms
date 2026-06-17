package com.pcms.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "inventory-service")
public interface InventoryOutboxClient {

        Logger log = LoggerFactory.getLogger(InventoryOutboxClient.class);

        @PostMapping("/inventory/orders/{orderId}/paid")
        @CircuitBreaker(name = "inventoryOutboxService", fallbackMethod = "fallbackOrderPaid")
        ResponseEntity<Object> orderPaid(@RequestHeader("X-Outbox-Event-Id") String eventId,
                        @PathVariable("orderId") UUID orderId,
                        @RequestBody Map<String, Object> payload);

        @PostMapping("/inventory/orders/{orderId}/cancelled")
        @CircuitBreaker(name = "inventoryOutboxService", fallbackMethod = "fallbackOrderCancelled")
        ResponseEntity<Object> orderCancelled(@RequestHeader("X-Outbox-Event-Id") String eventId,
                        @PathVariable("orderId") UUID orderId,
                        @RequestBody Map<String, Object> payload);

        default ResponseEntity<Object> fallbackOrderPaid(String eventId, UUID orderId, Map<String, Object> payload,
                        Throwable throwable) {
                log.warn("Inventory outbox paid fallback for order {}: {}", orderId, throwable.getMessage());
                return ResponseEntity.accepted().body(Map.of(
                                "status", "DEFERRED",
                                "eventId", eventId,
                                "orderId", orderId,
                                "message", "Inventory service unavailable, stock consumption deferred"));
        }

        default ResponseEntity<Object> fallbackOrderCancelled(String eventId, UUID orderId, Map<String, Object> payload,
                        Throwable throwable) {
                log.warn("Inventory outbox cancelled fallback for order {}: {}", orderId, throwable.getMessage());
                return ResponseEntity.accepted().body(Map.of(
                                "status", "DEFERRED",
                                "eventId", eventId,
                                "orderId", orderId,
                                "message", "Inventory service unavailable, stock restore deferred"));
        }
}