package com.pcms.inventoryservice.client;

import com.pcms.common.exception.DependencyUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

/**
 * Feign client for catalog-service (UC04).
 * Used to validate medicine existence + fetch name when importing stock.
 */
@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/medicines/{id}")
    @CircuitBreaker(name = "catalogService", fallbackMethod = "fallbackGetMedicine")
    Map<String, Object> getMedicineById(@PathVariable UUID id);

    default Map<String, Object> fallbackGetMedicine(UUID id, Throwable t) {
        Throwable current = t;
        while (current != null) {
            if (current instanceof FeignException.NotFound notFound) {
                throw notFound;
            }
            current = current.getCause();
        }

        log.warn("Catalog service unavailable for medicine {}: {}", id, t.getMessage());
        throw new DependencyUnavailableException(
                "Catalog service",
                "Không thể liên lạc với danh mục thuốc để kiểm tra thuốc. Vui lòng thử lại sau.");
    }

    // Allow log access without static import
    org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CatalogClient.class);
}
