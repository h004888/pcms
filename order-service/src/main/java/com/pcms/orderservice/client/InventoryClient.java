package com.pcms.orderservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
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

    /**
     * Aggregate available stock for a medicine at a branch.
     * Sums qtyOnHand across all non-expired batches via
     * GET /api/v1/inventory/branch/{branchId}/medicine/{medicineId}.
     * Used by OrderService.recompute() to surface stock conflicts.
     */
    @GetMapping("/inventory/branch/{branchId}/medicine/{medicineId}")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackGetAvailableStock")
    List<Map<String, Object>> getBatchesByBranchAndMedicine(
            @PathVariable UUID branchId,
            @PathVariable UUID medicineId);

    default Map<String, Object> getAvailableStock(UUID medicineId, UUID branchId) {
        List<Map<String, Object>> batches = getBatchesByBranchAndMedicine(branchId, medicineId);
        int totalQty = 0;
        if (batches != null) {
            for (Map<String, Object> b : batches) {
                Object qty = b.get("qtyOnHand");
                if (qty instanceof Number n) {
                    totalQty += n.intValue();
                }
            }
        }
        return Map.of("availableQty", totalQty, "medicineId", medicineId.toString(),
                "branchId", branchId.toString());
    }

    @SuppressWarnings("unused")
    default List<Map<String, Object>> fallbackGetAvailableStock(UUID branchId, UUID medicineId, Throwable t) {
        // Return empty list — OrderService.recompute() will emit INFO warning
        return List.of();
    }

    record ConsumeRequest(UUID medicineId, UUID branchId, Integer qty, UUID actorId, UUID orderId) {}

    default Map<String, Object> fallbackConsume(ConsumeRequest request, Throwable t) {
        // Log error; order will be paid but stock deduction deferred (eventual consistency)
        return Map.of("status", "DEFERRED", "message", "Inventory service unavailable, will retry later");
    }
}
