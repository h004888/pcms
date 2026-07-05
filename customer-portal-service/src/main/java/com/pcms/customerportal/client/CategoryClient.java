package com.pcms.customerportal.client;

import com.pcms.common.dto.PageResponse;
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
 * Feign client for category-service.
 * Used by customer-portal-service to enrich home page and PDP with category info.
 *
 * <p>Resilience: circuit breaker with fallback that returns empty list so
 * B2C pages degrade gracefully when category-service is down.
 */
@FeignClient(name = "category-service")
public interface CategoryClient {

    Logger log = LoggerFactory.getLogger(CategoryClient.class);

    @GetMapping("/categories")
    @CircuitBreaker(name = "categoryService", fallbackMethod = "fallbackList")
    PageResponse<Map<String, Object>> list(@RequestParam(name = "page", defaultValue = "0") int page,
                                          @RequestParam(name = "size", defaultValue = "50") int size);

    @GetMapping("/categories/{id}")
    @CircuitBreaker(name = "categoryService", fallbackMethod = "fallbackGetById")
    Map<String, Object> getById(@PathVariable("id") String id);

    default PageResponse<Map<String, Object>> fallbackList(int page, int size, Throwable throwable) {
        log.warn("category-service unavailable while listing categories: {}", throwable.getMessage());
        return PageResponse.empty(page, size);
    }

    default Map<String, Object> fallbackGetById(String id, Throwable throwable) {
        log.warn("category-service unavailable while fetching category {}: {}", id, throwable.getMessage());
        return Map.of();
    }
}