package com.pcms.customerportal.controller;

import com.pcms.customerportal.dto.request.InstallmentConfirmRequest;
import com.pcms.customerportal.dto.request.InstallmentQuoteRequest;
import com.pcms.customerportal.dto.response.InstallmentConfirmResponse;
import com.pcms.customerportal.dto.response.InstallmentQuoteResponse;
import com.pcms.customerportal.service.InstallmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SHOP-INSTALLMENT - Installment purchase (2 endpoints).
 * Providers: Home Credit (1.5%/month), FE Credit (1.2%/month).
 */
@RestController
@RequestMapping("/installment")
@Tag(name = "UC14/UC19 - Installment")
public class InstallmentController {

    private final InstallmentService service;

    public InstallmentController(InstallmentService service) {
        this.service = service;
    }

    @PostMapping("/quote")
    @Operation(summary = "Get installment quote for an amount/months combination")
    public ResponseEntity<InstallmentQuoteResponse> quote(
            @Valid @RequestBody InstallmentQuoteRequest request) {
        return ResponseEntity.ok(service.quote(request));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirm installment application (stub - real provider integration in future)")
    public ResponseEntity<InstallmentConfirmResponse> confirm(
            @Valid @RequestBody InstallmentConfirmRequest request) {
        return ResponseEntity.ok(service.confirm(request));
    }
}