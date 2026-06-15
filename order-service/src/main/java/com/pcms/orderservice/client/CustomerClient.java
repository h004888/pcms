package com.pcms.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;

/**
 * Feign client for customer-service (UC08)
 * Used to validate customer, award loyalty points
 */
@FeignClient(name = "customer-service")
public interface CustomerClient {

    @GetMapping("/customers/{id}")
    @CircuitBreaker(name = "customerService", fallbackMethod = "fallbackGetCustomer")
    Map<String, Object> getCustomerById(@PathVariable UUID id);

    @PutMapping("/customers/{id}/points/add")
    @CircuitBreaker(name = "customerService", fallbackMethod = "fallbackAddPoints")
    Map<String, Object> addPoints(@PathVariable UUID id, @RequestBody Map<String, Integer> body);

    default Map<String, Object> fallbackGetCustomer(UUID id, Throwable t) {
        return Map.of("id", id, "name", "Unknown Customer");
    }

    default Map<String, Object> fallbackAddPoints(UUID id, Map<String, Integer> body, Throwable t) {
        return Map.of("status", "DEFERRED", "message", "Customer service unavailable, loyalty points will be added later");
    }
}
