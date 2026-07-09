package com.pcms.customerservice.controller;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerservice.client.OrderClient;
import com.pcms.customerservice.dto.request.AddPointsRequest;
import com.pcms.customerservice.dto.request.CreateCustomerRequest;
import com.pcms.customerservice.dto.request.UpdateCustomerRequest;
import com.pcms.customerservice.dto.response.CustomerHistoryResponse;
import com.pcms.customerservice.dto.response.CustomerOrderSummaryResponse;
import com.pcms.customerservice.dto.response.CustomerResponse;
import com.pcms.customerservice.dto.response.LoyaltyTransactionResponse;
import com.pcms.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * UC08 - Customer Profile & Loyalty
 * BR07: Award loyalty points when order is paid
 */
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final OrderClient orderClient;

    public CustomerController(CustomerService customerService, OrderClient orderClient) {
        this.customerService = customerService;
        this.orderClient = orderClient;
    }

    @GetMapping
    public ResponseEntity<PageResponse<CustomerResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.of(customerService.list(search, page, size), c -> c));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<CustomerResponse> getByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(customerService.getByPhone(phone));
    }

    /**
     * GET /api/v1/customers/code/{code} - TICKET-105 (FR8.2).
     * Looks up a customer by the auto-generated CUST-yyyy#### code
     * (displayed on membership cards and printed receipts). Returns 404
     * with MSG31 if the code is unknown.
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<CustomerResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(customerService.getByCode(code));
    }

    @GetMapping("/{id}/tier")
    public ResponseEntity<Map<String, Object>> getTier(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("customerId", id, "tier", customerService.getTier(id)));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<PageResponse<CustomerOrderSummaryResponse>> getOrders(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(customerService.getOrders(id, page, size));
    }

    @GetMapping("/{id}/points")
    public ResponseEntity<PageResponse<LoyaltyTransactionResponse>> getPoints(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(customerService.getPoints(id, page, size));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<CustomerHistoryResponse> getHistory(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(customerService.getHistory(id, page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        customerService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/customers - Step 5-9 of UC08 main flow.
     * Auto-generates code CUST-yyyy####
     */
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        customerService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/v1/customers/{id}/points/add - Called by payment-service (BR07)
     * Award 1 point per 1000 VND of order total.
     *
     * <p>Idempotent: if {@code refOrderId} was already processed, the call
     * returns the current customer state without changing points.
     *
     * <p>Request body:
     * <pre>{@code
     * {
     * "points": 5, // can be negative
     * "refOrderId": "uuid", // optional but recommended
     * "reason": "ORDER_PAID:ORD-20260616-0001" // optional
     * }
     * }</pre>
     */
    @PutMapping("/{id}/points/add")
    public ResponseEntity<Map<String, Object>> addPoints(@PathVariable UUID id,
            @Valid @RequestBody AddPointsRequest body) {
        CustomerResponse c = customerService.addPoints(id,
                body.points() == null ? 0 : body.points(),
                body.refOrderId(),
                body.reason());
        return ResponseEntity.ok(Map.of(
                "customerId", c.id(),
                "addedPoints", body.points() == null ? 0 : body.points(),
                "totalPoints", c.points()));
    }
}
