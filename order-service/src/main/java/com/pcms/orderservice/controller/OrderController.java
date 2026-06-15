package com.pcms.orderservice.controller;

import com.pcms.orderservice.entity.Order;
import com.pcms.orderservice.enums.OrderStatus;
import com.pcms.orderservice.repository.OrderRepository;
import com.pcms.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * UC06 - Manage Orders
 * Authorization: Pharmacist + Admin/CEO/Manager, Customer (own orders only)
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        Page<Order> orders;
        if (customerId != null) {
            orders = orderRepository.findByCustomerId(customerId, pageable);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return ResponseEntity.ok(Map.of(
            "data", orders.getContent(),
            "page", orders.getNumber(),
            "size", orders.getSize(),
            "total", orders.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable UUID id) {
        return orderRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getByNumber(@PathVariable String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/orders - Step 1-12 of UC06 main flow
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(
                request.customerId(),
                request.branchId(),
                request.staffId(),
                request.items(),
                request.couponCode()
            );
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG33", "message", e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/orders/{id}/pay - Called by payment-service after success
     */
    @PutMapping("/{id}/pay")
    public ResponseEntity<?> markAsPaid(@PathVariable UUID id, @RequestParam(required = false) UUID actorId) {
        try {
            Order order = orderService.markAsPaid(id, actorId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/orders/{id} - AT2 of UC06: Cancel order
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable UUID id, @RequestParam(required = false) UUID actorId) {
        try {
            Order order = orderService.cancelOrder(id, actorId);
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("code", "MSG19", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    public record CreateOrderRequest(UUID customerId, UUID branchId, UUID staffId, List<Map<String, Object>> items, String couponCode) {}
}
