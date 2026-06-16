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
 * Feign client for branch-service (UC03) — used by Order to validate branch existence (B-21).
 */
@FeignClient(name = "branch-service")
public interface BranchClient {

    Logger log = LoggerFactory.getLogger(BranchClient.class);

    @GetMapping("/branches/{id}")
    @CircuitBreaker(name = "branchService", fallbackMethod = "fallbackGetBranch")
    Map<String, Object> getBranchById(@PathVariable UUID id);

    default Map<String, Object> fallbackGetBranch(UUID id, Throwable t) {
        log.warn("Branch service unavailable for branch {}: {}", id, t.getMessage());
        return Map.of("id", id.toString(), "name", "Unknown Branch", "status", "UNREACHABLE");
    }
}
