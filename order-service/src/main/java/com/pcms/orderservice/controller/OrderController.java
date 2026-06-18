package com.pcms.orderservice.controller;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.orderservice.dto.CreateOrderRequest;
import com.pcms.orderservice.dto.OrderResponse;
import com.pcms.orderservice.dto.PageResponse;
import com.pcms.orderservice.dto.UpdateOrderRequest;
import com.pcms.orderservice.enums.OrderStatus;
import com.pcms.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * UC06 - Manage Orders
 * Authorization: Pharmacist + Admin/CEO/Manager, Customer (own orders only)
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> list(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.of(
                orderService.list(customerId, status, branchId, dateFrom, dateTo, page, size), o -> o));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getByNumber(orderNumber));
    }

    /**
     * POST /api/v1/orders - Step 1-12 of UC06 main flow
     */
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    /**
     * PUT /api/v1/orders/{id} - Update a PENDING_PAYMENT order's items.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    /**
     * PUT /api/v1/orders/{id}/pay - Called by payment-service after success
     */
    @PutMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> markAsPaid(@PathVariable UUID id,
            @RequestParam(required = false) UUID actorId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(orderService.markAsPaid(id, resolveActorId(actorId, userId)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<OrderResponse> approve(@PathVariable UUID id,
            @RequestParam(required = false) UUID actorId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(orderService.approve(id, resolveActorId(actorId, userId)));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<OrderResponse> reject(@PathVariable UUID id,
            @RequestParam(required = false) UUID actorId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(orderService.reject(id, resolveActorId(actorId, userId)));
    }

    /**
     * DELETE /api/v1/orders/{id} - AT2 of UC06: Cancel order
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> cancel(@PathVariable UUID id,
            @RequestParam(required = false) UUID actorId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(orderService.cancel(id, resolveActorId(actorId, userId)));
    }

    private UUID resolveActorId(UUID actorId, UUID userId) {
        if (actorId != null) {
            return actorId;
        }
        if (userId != null) {
            return userId;
        }
        throw new InvalidOperationException(
                "Actor ID is required",
                "Thiếu thông tin người thao tác hiện tại");
    }
}
