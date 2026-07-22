package com.pcms.customerportal.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
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

    /**
     * POST /orders — create a new order.
     * Used by CheckoutService to create an order from the B2C cart.
     */
    @PostMapping("/orders")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackCreateOrder")
    Map<String, Object> createOrder(@RequestBody Map<String, Object> orderRequest);

    default Map<String, Object> fallbackCreateOrder(Map<String, Object> orderRequest, Throwable t) {
        log.error("order-service unavailable while creating order: {}", t.getMessage());
        return Map.of("id", "",
                      "orderNumber", "",
                      "status", "FAILED",
                      "message", "Order service temporarily unavailable");
    }

    /**
     * GET /orders/{id} — get order by ID.
     * Used by OrderTrackingService to fetch order details for tracking.
     */
    @GetMapping("/orders/{id}")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackGetById")
    Map<String, Object> getById(@PathVariable("id") String orderId);

    default Map<String, Object> fallbackGetById(String orderId, Throwable t) {
        throw new IllegalStateException("order-service unavailable while fetching order " + orderId, t);
    }

    /**
     * GET /orders/number/{orderNumber} — get order by number.
     * Used by OrderTrackingService to fetch order details by order number.
     */
    @GetMapping("/orders/number/{orderNumber}")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackGetByNumber")
    Map<String, Object> getByNumber(@PathVariable("orderNumber") String orderNumber);

    default Map<String, Object> fallbackGetByNumber(String orderNumber, Throwable t) {
        log.warn("order-service unavailable while fetching order {}: {}", orderNumber, t.getMessage());
        return Map.of();
    }

    /**
     * GET /orders?customerId=...&status=...&page=0&size=20
     * Used by OrderTrackingService to list order history with pagination.
     */
    @GetMapping("/orders")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackListForHistory")
    Map<String, Object> listForHistory(
            @RequestParam(name = "customerId") String customerId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "dateFrom", required = false) String dateFrom,
            @RequestParam(name = "dateTo", required = false) String dateTo,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size);

    default Map<String, Object> fallbackListForHistory(String customerId, String status, String dateFrom,
                                                        String dateTo, int page, int size, Throwable t) {
        throw new IllegalStateException("order-service unavailable while listing orders for " + customerId, t);
    }

    @GetMapping("/orders/{id}/status-history")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackGetStatusHistory")
    List<Map<String, Object>> getStatusHistory(@PathVariable("id") String orderId);

    default List<Map<String, Object>> fallbackGetStatusHistory(String orderId, Throwable t) {
        throw new IllegalStateException("order-service unavailable while fetching status history for " + orderId, t);
    }

    /**
     * GET /payments/order/{orderId} — get payment by order ID.
     * Used to get paymentUrl for checkout confirm response.
     */
    @GetMapping("/payments/order/{orderId}")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackGetPaymentByOrder")
    Map<String, Object> getPaymentByOrder(@PathVariable("orderId") String orderId);

    default Map<String, Object> fallbackGetPaymentByOrder(String orderId, Throwable t) {
        log.warn("order-service unavailable while fetching payment for order {}: {}", orderId, t.getMessage());
        return Map.of();
    }

    /**
     * GET /orders/analytics/top-medicines — top N best-selling medicines.
     * Used by SHOP-HOME best-sellers section.
     */
    @GetMapping("/orders/analytics/top-medicines")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackTopMedicines")
    List<Map<String, Object>> getTopMedicines(
            @RequestParam(name = "periodDays", defaultValue = "30") int periodDays,
            @RequestParam(name = "limit", defaultValue = "10") int limit);

    default List<Map<String, Object>> fallbackTopMedicines(int periodDays, int limit, Throwable t) {
        log.warn("order-service unavailable for top-medicines: {}", t.getMessage());
        return List.of();
    }

    @PostMapping("/orders/{id}/cancel")
    Map<String, Object> cancelOrder(@PathVariable("id") String orderId,
            @RequestParam("actorId") String actorId);
}
