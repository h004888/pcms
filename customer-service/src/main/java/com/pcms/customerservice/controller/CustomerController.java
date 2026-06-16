package com.pcms.customerservice.controller;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.common.dto.PageResponse;
import com.pcms.customerservice.dto.CreateCustomerRequest;
import com.pcms.customerservice.dto.CustomerResponse;
import com.pcms.customerservice.dto.UpdateCustomerRequest;
import com.pcms.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
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
        // Lookup by phone is not in the interface; delegate via repository through service: reuse list()
        // For a single hit we expose the first match.
        return customerService.list(phone, 0, 1).getContent().stream()
            .filter(c -> c.phone() != null && c.phone().contains(phone))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("Customer with phone", phone));
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
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
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
     *   "points": 5,                        // can be negative
     *   "refOrderId": "uuid",               // optional but recommended
     *   "reason": "ORDER_PAID:ORD-20260616-0001"  // optional
     * }
     * }</pre>
     */
    @PutMapping("/{id}/points/add")
    public ResponseEntity<Map<String, Object>> addPoints(@PathVariable UUID id,
                                                         @RequestBody AddPointsRequest body) {
        CustomerResponse c = customerService.addPoints(id,
                body.points() == null ? 0 : body.points(),
                body.refOrderId(),
                body.reason());
        return ResponseEntity.ok(Map.of(
                "customerId", c.id(),
                "addedPoints", body.points() == null ? 0 : body.points(),
                "totalPoints", c.points()
        ));
    }

    /** Request body for /points/add endpoint. */
    public record AddPointsRequest(Integer points, UUID refOrderId, String reason) {}
}
