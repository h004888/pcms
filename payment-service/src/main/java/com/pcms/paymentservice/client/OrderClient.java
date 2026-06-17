package com.pcms.paymentservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Feign client for order-service (UC06)
 * Called to mark order as paid after successful payment
 */
@FeignClient(name = "order-service")
public interface OrderClient {

    @PutMapping("/orders/{id}/pay")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackMarkPaid")
    Map<String, Object> markOrderPaid(@PathVariable UUID id,
            @RequestParam(name = "actorId", required = false) UUID actorId);

    default Map<String, Object> fallbackMarkPaid(UUID id, UUID actorId, Throwable t) {
        return Map.of("status", "DEFERRED", "message", "Order service unavailable, payment recorded");
    }
}
