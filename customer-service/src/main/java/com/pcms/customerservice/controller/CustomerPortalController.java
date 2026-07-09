package com.pcms.customerservice.controller;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.customerservice.dto.request.CustomerPortalRegisterRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerPortalRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> me(
            @RequestHeader(value = "X-Customer-Id", required = false) UUID customerId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestParam(name = "customerId", required = false) UUID fallbackCustomerId) {
        return ResponseEntity.ok(resolveCurrentCustomer(customerId, userEmail, fallbackCustomerId));
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMe(
            @RequestHeader(value = "X-Customer-Id", required = false) UUID customerId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestParam(name = "customerId", required = false) UUID fallbackCustomerId,
            @Valid @RequestBody CustomerPortalRegisterRequest request) {
        if (customerId != null) {
            return ResponseEntity.ok(customerService.updatePortalProfile(customerId, request));
        }
        if (userEmail != null && !userEmail.isBlank()) {
            return ResponseEntity.ok(customerService.updatePortalProfileByEmail(userEmail, request));
        }
        validateFallbackCustomerId(fallbackCustomerId);
        return ResponseEntity.ok(customerService.updatePortalProfile(fallbackCustomerId, request));
    }

    private CustomerResponse resolveCurrentCustomer(UUID customerId, String userEmail, UUID fallbackCustomerId) {
        if (customerId != null) {
            return customerService.getById(customerId);
        }
        if (userEmail != null && !userEmail.isBlank()) {
            return customerService.getByEmail(userEmail);
        }
        validateFallbackCustomerId(fallbackCustomerId);
        return customerService.getById(fallbackCustomerId);
    }

    private void validateFallbackCustomerId(UUID fallbackCustomerId) {
        if (fallbackCustomerId == null) {
            throw new InvalidOperationException(
                    "Customer self-context is missing",
                    "Thiếu thông tin định danh khách hàng hiện tại");
        }
    }
}
