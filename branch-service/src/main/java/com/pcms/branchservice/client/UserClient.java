package com.pcms.branchservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {

    Logger log = LoggerFactory.getLogger(UserClient.class);

    @GetMapping("/users/{id}")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUser")
    Map<String, Object> getUserById(@PathVariable UUID id);

    @GetMapping("/users")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackListUsers")
    Map<String, Object> listUsers(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "branchId", required = false) UUID branchId,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size);

    default Map<String, Object> fallbackGetUser(UUID id, Throwable throwable) {
        log.warn("User service unavailable for user {}: {}", id, throwable.getMessage());
        return Map.of("id", id.toString(), "status", "UNREACHABLE");
    }

    default Map<String, Object> fallbackListUsers(String search, String role, String status, UUID branchId,
            int page, int size, Throwable throwable) {
        log.warn("User service unavailable for branch staff {}: {}", branchId, throwable.getMessage());
        return Map.of("data", java.util.List.of());
    }
}