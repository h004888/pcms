package com.pcms.catalogservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    Logger log = LoggerFactory.getLogger(InventoryClient.class);

    @GetMapping("/inventory")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackGetInventory")
    List<Map<String, Object>> getInventory(@RequestParam(name = "branchId", required = false) UUID branchId);

    default List<Map<String, Object>> fallbackGetInventory(UUID branchId, Throwable throwable) {
        log.warn("Inventory service unavailable while searching in-stock medicines: {}", throwable.getMessage());
        return List.of();
    }
}