package com.pcms.prescriptionservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {

    Logger log = LoggerFactory.getLogger(UserClient.class);

    @GetMapping("/users/{id}")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUser")
    Map<String, Object> getUserById(@PathVariable UUID id);

    default Map<String, Object> fallbackGetUser(UUID id, Throwable throwable) {
        log.warn("User service unavailable for doctor {}: {}", id, throwable.getMessage());
        return Map.of("id", id.toString(), "status", "UNREACHABLE");
    }
}