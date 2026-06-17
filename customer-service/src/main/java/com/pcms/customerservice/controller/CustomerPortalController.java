package com.pcms.customerservice.controller;

import com.pcms.customerservice.dto.CreateCustomerRequest;
import com.pcms.customerservice.dto.CustomerResponse;
import com.pcms.customerservice.dto.UpdateCustomerRequest;
import com.pcms.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * B11: Self-service portal for customers.
 * Public routes — no auth required for register; /me uses header X-Customer-Id.
 */
@RestController
@RequestMapping("/customers")
public class CustomerPortalController {

    private final CustomerService customerService;

    public CustomerPortalController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /** POST /customers/register — public self-registration. */
    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    /** GET /customers/me — get own profile by id header. */
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMe(@RequestHeader("X-Customer-Id") UUID customerId) {
        return ResponseEntity.ok(customerService.getById(customerId));
    }

    /** PUT /customers/me — update own profile. */
    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMe(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(customerService.update(customerId, request));
    }
}
