package com.pcms.catalogservice.controller;

import com.pcms.catalogservice.dto.request.CreateMedicineRequest;
import com.pcms.catalogservice.dto.request.UpdateMedicineRequest;
import com.pcms.catalogservice.dto.response.MedicineResponse;
import com.pcms.catalogservice.dto.response.PageResponse;
import com.pcms.catalogservice.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * UC04 - Manage Medicines
 * UC10 - Search Medicines (SCR-SEARCH, FR10.1-FR10.4)
 */
@RestController
@RequestMapping("/medicines")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<MedicineResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                medicineService.list(search, categoryId, minPrice, maxPrice, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(medicineService.getById(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<MedicineResponse> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(medicineService.getBySku(sku));
    }

    @PostMapping
    public ResponseEntity<MedicineResponse> create(@Valid @RequestBody CreateMedicineRequest request) {
        return ResponseEntity.ok(medicineService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineResponse> update(@PathVariable UUID id,
                                                    @Valid @RequestBody UpdateMedicineRequest request) {
        return ResponseEntity.ok(medicineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        medicineService.softDelete(id);
        return ResponseEntity.ok().build();
    }
}
