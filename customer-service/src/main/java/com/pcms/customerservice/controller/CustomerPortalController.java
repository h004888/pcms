package com.pcms.customerservice.controller;

import com.pcms.customerservice.dto.request.CustomerPortalRegisterRequest;
import com.pcms.customerservice.dto.request.CustomerProvisionRequest;
import com.pcms.customerservice.dto.response.CustomerResponse;
import com.pcms.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

/**
 * B11: Self-service portal for customers.
 * Public routes — no auth required for register; /me uses header X-User-Id.
 */
@RestController
@RequestMapping("/customers")
public class CustomerPortalController {

    private final CustomerService customerService;

    public CustomerPortalController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerPortalRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(request));
    }

    /**
     * Internal provisioning endpoint called by user-service via Feign.
     * userId is the canonical B2C customer identity.
     */
    @PostMapping("/internal/provision")
    public ResponseEntity<CustomerResponse> provision(@Valid @RequestBody CustomerProvisionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.provisionFromUser(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> me(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(customerService.getById(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMe(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CustomerPortalRegisterRequest request) {
        return ResponseEntity.ok(customerService.updatePortalProfile(userId, request));
    }
}
