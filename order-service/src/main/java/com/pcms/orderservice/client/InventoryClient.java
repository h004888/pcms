package com.pcms.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;

/**
 * Feign client for inventory-service (UC05)
 * Called when order is paid to deduct stock (NSF-05 FIFO)
 */
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/inventory/consume")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackConsume")
    Map<String, Object> consumeStock(@RequestBody ConsumeRequest request);

    record ConsumeRequest(UUID medicineId, UUID branchId, Integer qty, UUID actorId, UUID orderId) {}

    default Map<String, Object> fallbackConsume(ConsumeRequest request, Throwable t) {
        // Log error; order will be paid but stock deduction deferred (eventual consistency)
        return Map.of("status", "DEFERRED", "message", "Inventory service unavailable, will retry later");
    }
}
