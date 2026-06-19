package com.pcms.orderservice.dto;

import com.pcms.orderservice.entity.Order;
import com.pcms.orderservice.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for POST /orders/{id}/recompute.
 *
 * <p>
 * Re-applies BR04 bulk discount (5% when qty >= 10 same medicine) and
 * checks stock availability via inventory-service.
 *
 * <p>
 * Used by SCR-ORDER-NEW (SRS UC06) when a pharmacist changes quantity or
 * before placing an order — surfaces stock conflicts so the user can
 * re-quote.
 *
 * @param subtotal       sum of line subtotals (no discounts)
 * @param discount       BR04 discount total (5% per line with qty >= 10)
 * @param total          subtotal - discount
 * @param status         order status snapshot at recompute time
 * @param stockWarnings  per-line stock conflicts (empty if all available)
 */
public record OrderRecomputeResponse(
        UUID orderId,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        OrderStatus status,
        List<StockWarning> stockWarnings) {

    /**
     * Per-line stock conflict. Returned when requested qty exceeds
     * available qty in any FIFO batch for the branch.
     */
    public record StockWarning(
            UUID medicineId,
            String medicineName,
            int requestedQty,
            int availableQty,
            String severity // "INFO" | "WARNING" | "BLOCK"
    ) {}

    public static OrderRecomputeResponse from(Order o, List<StockWarning> warnings) {
        return new OrderRecomputeResponse(
                o.getId(),
                o.getSubtotal(),
                o.getDiscount(),
                o.getTotal(),
                o.getStatus(),
                warnings);
    }
}