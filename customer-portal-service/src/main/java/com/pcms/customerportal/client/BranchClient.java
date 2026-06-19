package com.pcms.customerportal.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Feign client for branch-service. Used by B2C store locator to enumerate
 * pharmacy branches.
 *
 * <p>Per SDS section 4.14, distance ranking is intended to be done with
 * PostGIS, but for the MVP we just list and let the UI sort.
 */
@FeignClient(name = "branch-service")
public interface BranchClient {

    Logger log = LoggerFactory.getLogger(BranchClient.class);

    @GetMapping("/branches")
    @CircuitBreaker(name = "branchService", fallbackMethod = "fallbackList")
    List<Map<String, Object>> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                   @RequestParam(name = "size", defaultValue = "50") int size);

    @GetMapping("/branches/{id}")
    @CircuitBreaker(name = "branchService", fallbackMethod = "fallbackGetById")
    Map<String, Object> getById(@PathVariable("id") String id);

    default List<Map<String, Object>> fallbackList(int page, int size, Throwable throwable) {
        log.warn("branch-service unavailable while listing: {}", throwable.getMessage());
        return List.of();
    }

    default Map<String, Object> fallbackGetById(String id, Throwable throwable) {
        log.warn("branch-service unavailable while fetching branch {}: {}", id, throwable.getMessage());
        return Map.of();
    }
}
