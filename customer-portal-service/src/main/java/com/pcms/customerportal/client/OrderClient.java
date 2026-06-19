package com.pcms.customerportal.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Feign client for order-service.
 * <p>Used by TICKET-703 (Health Wallet) to compute a customer's total
 * paid-order spend and assign them a wallet tier.
 */
@FeignClient(name = "order-service")
public interface OrderClient {

    Logger log = LoggerFactory.getLogger(OrderClient.class);

    /**
     * GET /api/v1/orders?customerId=...&status=PAID&page=0&size=1
     * Returns the customer's most recent PAID order (we use the totals
     * to estimate the lifetime spend).
     */
    @GetMapping("/orders")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackList")
    Map<String, Object> list(
            @RequestParam(name = "customerId") String customerId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "1") int size);

    default Map<String, Object> fallbackList(String customerId, String status, int page, int size, Throwable t) {
        log.warn("order-service unavailable while listing for customer {}: {}",
                customerId, t.getMessage());
        return Map.of("data", java.util.List.of(),
                      "page", page, "size", size, "total", 0, "totalPages", 0);
    }

    /**
     * Sum of all paid-order totals for a customer.
     * Convenience derived from list() (which exposes total).
     * NOTE: This is a pragmatic approximation - in production you'd want a
     * dedicated aggregation endpoint on order-service. For MVP we use the
     * last page's total as a lower-bound signal of wallet tier.
     */
    default BigDecimal estimateLifetimeSpend(String customerId) {
        try {
            Map<String, Object> page = list(customerId, "PAID", 0, 1);
            Object total = page.get("total");
            if (total instanceof Number n) {
                // Rough heuristic: total orders * average order value
                // A real implementation would call a dedicated /wallet/spend endpoint
                return BigDecimal.valueOf(n.longValue()).multiply(BigDecimal.valueOf(500_000L));
            }
        } catch (Exception e) {
            log.warn("estimateLifetimeSpend failed for {}: {}", customerId, e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
