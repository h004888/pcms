package com.pcms.pharmacistworkbench.controller;

import com.pcms.pharmacistworkbench.dto.response.CustomerProfile360Response;
import com.pcms.pharmacistworkbench.service.Customer360Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/rx")
@Tag(name = "UC16 - Pharmacist Workbench / Customer 360")
public class Customer360Controller {

    private final Customer360Service service;

    public Customer360Controller(Customer360Service service) {
        this.service = service;
    }

    @GetMapping("/customers/{id}/profile-360")
    @Operation(summary = "Get full customer 360° profile (RX-CUST-PROFILE-360)")
    public ResponseEntity<CustomerProfile360Response> get360(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get360(id));
    }
}
