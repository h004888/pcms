package com.pcms.ecomops.controller;

import com.pcms.ecomops.dto.request.CreateFlashSaleRequest;
import com.pcms.ecomops.dto.response.FlashSaleResponse;
import com.pcms.ecomops.service.FlashSaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/flash-sales")
@Tag(name = "UC19 - E-commerce Ops (Flash Sales)")
public class FlashSaleController {

    private final FlashSaleService service;

    public FlashSaleController(FlashSaleService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a new flash sale (admin only)")
    public ResponseEntity<FlashSaleResponse> create(@Valid @RequestBody CreateFlashSaleRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping("/active")
    @Operation(summary = "List active flash sales (public) - SHOP-FLASH-SALE")
    public ResponseEntity<List<FlashSaleResponse>> listActive() {
        return ResponseEntity.ok(service.listActive());
    }

    @GetMapping
    @Operation(summary = "List all flash sales (admin)")
    public ResponseEntity<List<FlashSaleResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flash sale by id")
    public ResponseEntity<FlashSaleResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a flash sale")
    public ResponseEntity<FlashSaleResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(service.cancel(id));
    }
}
