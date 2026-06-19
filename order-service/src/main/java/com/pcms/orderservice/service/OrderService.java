package com.pcms.orderservice.service;

import com.pcms.orderservice.dto.CreateOrderRequest;
import com.pcms.orderservice.dto.OrderRecomputeResponse;
import com.pcms.orderservice.dto.OrderResponse;
import com.pcms.orderservice.dto.UpdateOrderRequest;
import com.pcms.orderservice.enums.OrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service interface for Order operations (UC06).
 *
 * <p>
 * BR04: 5% discount when qty >= 10 same medicine.
 * <br>
 * BR01: Auto-cancel pending orders after 24h.
 * <br>
 * NSF-05: FIFO batch consumption via inventory-service.
 * <br>
 * NSF-12: Generate order number ORD-yyyymmdd-####.
 */
public interface OrderService {

    /** Paginated list. Optionally filtered by customerId or status. */
    Page<OrderResponse> list(UUID customerId, OrderStatus status, int page, int size);

    Page<OrderResponse> list(UUID customerId, OrderStatus status, UUID branchId,
            LocalDate dateFrom, LocalDate dateTo, int page, int size);

    OrderResponse getById(UUID id);

    OrderResponse getByNumber(String orderNumber);

    /** Step 5-12 of UC06 main flow. */
    OrderResponse create(CreateOrderRequest request);

    /** Update items on a PENDING_PAYMENT order. */
    OrderResponse update(UUID id, UpdateOrderRequest request);

    /** Called by payment-service after payment success. */
    OrderResponse markAsPaid(UUID orderId, UUID actorId);

    /** Manager approval workflow. */
    OrderResponse approve(UUID orderId, UUID actorId);

    /** Manager rejection workflow. */
    OrderResponse reject(UUID orderId, UUID actorId);

    /** AT2 of UC06: Cancel order; restore stock if previously PAID (BR06). */
    OrderResponse cancel(UUID orderId, UUID actorId);

    /**
     * Re-apply BR04 bulk discount and check stock availability for a
     * PENDING_PAYMENT order (SDD §6.8 POST /orders/{id}/recompute).
     *
     * <p>
     * Does NOT persist. Returns recomputed totals plus per-line stock
     * warnings so the caller can decide whether to adjust quantities
     * before placing the order.
     */
    OrderRecomputeResponse recompute(UUID orderId);
}
