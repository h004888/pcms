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
 * Feign client for inventory-service. Used to display real-time stock per
 * branch on the B2C product detail page.
 */
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    Logger log = LoggerFactory.getLogger(InventoryClient.class);

    @GetMapping("/inventory/branch/{branchId}/medicine/{medicineId}")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackGetStock")
    List<Map<String, Object>> getStockByBranchAndMedicine(
            @PathVariable("branchId") String branchId,
            @PathVariable("medicineId") String medicineId);

    @GetMapping("/inventory")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackListInventory")
    List<Map<String, Object>> list(@RequestParam(name = "branchId", required = false) String branchId);

    default List<Map<String, Object>> fallbackGetStock(String branchId, String medicineId, Throwable throwable) {
        log.warn("inventory-service unavailable for branch={} medicine={}: {}",
                branchId, medicineId, throwable.getMessage());
        return List.of();
    }

    default List<Map<String, Object>> fallbackListInventory(String branchId, Throwable throwable) {
        log.warn("inventory-service unavailable while listing: {}", throwable.getMessage());
        return List.of();
    }
}
