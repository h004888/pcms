package com.pcms.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "customer-service")
public interface CustomerPointsClient {

    Logger log = LoggerFactory.getLogger(CustomerPointsClient.class);

    @PutMapping("/customers/{customerId}/points/add")
    @CircuitBreaker(name = "customerPointsService", fallbackMethod = "fallbackAddPoints")
    ResponseEntity<Object> addPoints(@RequestHeader("X-Outbox-Event-Id") String eventId,
            @PathVariable("customerId") UUID customerId,
            @RequestBody Map<String, Object> payload);

    default ResponseEntity<Object> fallbackAddPoints(String eventId, UUID customerId, Map<String, Object> payload,
            Throwable t) {
        log.warn("Customer points fallback for customer {}: {}", customerId, t.getMessage());
        return ResponseEntity.accepted().body(Map.of(
                "status", "DEFERRED",
                "eventId", eventId,
                "customerId", customerId,
                "message", "Customer service unavailable, loyalty points deferred"));
    }
}