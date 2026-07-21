package com.pcms.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

/**
 * Feign client for user-service — used by Order to resolve staff display name.
 */
@FeignClient(name = "user-service")
public interface StaffClient {

    Logger log = LoggerFactory.getLogger(StaffClient.class);

    @GetMapping("/users/{id}")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetStaff")
    Map<String, Object> getStaffById(@PathVariable UUID id);

    default Map<String, Object> fallbackGetStaff(UUID id, Throwable t) {
        log.warn("User service unavailable for staff {}: {}", id, t.getMessage());
        return Map.of("id", id.toString(), "fullName", "Unknown Staff");
    }
}
