package com.pcms.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Feign client for catalog-service (UC04)
 * Used during order creation to fetch medicine price (FIFO handled by inventory)
 */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/medicines/{id}")
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackGetMedicine")
    Map<String, Object> getMedicineById(@PathVariable java.util.UUID id);

    default Map<String, Object> fallbackGetMedicine(java.util.UUID id, Throwable t) {
        return Map.of(
            "id", id,
            "name", "Unknown Medicine",
            "price", BigDecimal.ZERO,
            "prescriptionRequired", false
        );
    }
}
