package com.pcms.ecomops.controller;

import com.pcms.ecomops.dto.response.FlashSaleResponse;
import com.pcms.ecomops.service.FlashSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public B2C endpoints for flash sales (SHOP-FLASH-SALE).
 * Aligned to /api/v1/ecom-ops prefix.
 */
@RestController
@RequestMapping("/ecom-ops/flash-sales")
@Tag(name = "UC19 - E-commerce Ops (Public Flash Sales)")
public class EcomFlashSaleController {

    private final FlashSaleService service;

    public EcomFlashSaleController(FlashSaleService service) {
        this.service = service;
    }

    @GetMapping("/active")
    @Operation(summary = "List active flash sales for customers (SHOP-FLASH-SALE)")
    public ResponseEntity<List<FlashSaleResponse>> listActive() {
        return ResponseEntity.ok(service.listActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flash sale details for customers")
    public ResponseEntity<FlashSaleResponse> get(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(service.get(id));
    }
}
