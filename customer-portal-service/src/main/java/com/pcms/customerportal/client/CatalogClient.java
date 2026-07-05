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
 * Feign client for catalog-service. Used to fetch medicine data for PDP
 * and to proxy search queries from the B2C portal.
 *
 * <p>Note: page/size honored but service returns Map for flexibility. A
 * typed DTO can be introduced in a follow-up sprint if contract stabilizes.
 */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

    Logger log = LoggerFactory.getLogger(CatalogClient.class);

    @GetMapping("/medicines/{id}")
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackGetById")
    Map<String, Object> getById(@PathVariable("id") String id);

    @GetMapping("/search/medicines")
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackSearch")
    List<Map<String, Object>> searchMedicines(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size);

    default Map<String, Object> fallbackGetById(String id, Throwable throwable) {
        log.warn("catalog-service unavailable while fetching medicine {}: {}", id, throwable.getMessage());
        return Map.of();
    }

    default List<Map<String, Object>> fallbackSearch(String q, int page, int size, Throwable throwable) {
        log.warn("catalog-service unavailable while searching '{}': {}", q, throwable.getMessage());
        return List.of();
    }
}
