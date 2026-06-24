package com.pcms.catalogservice.controller;

import com.pcms.catalogservice.dto.request.CreateMedicineRequest;
import com.pcms.catalogservice.dto.request.UpdateMedicineRequest;
import com.pcms.catalogservice.dto.response.MedicineResponse;
import com.pcms.catalogservice.dto.response.PageResponse;
import com.pcms.catalogservice.service.ImageStorageService;
import com.pcms.catalogservice.service.MedicineExportService;
import com.pcms.catalogservice.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final ImageStorageService imageStorageService;
    private final MedicineExportService medicineExportService;

    public MedicineController(MedicineService medicineService,
            ImageStorageService imageStorageService,
            MedicineExportService medicineExportService) {
        this.medicineService = medicineService;
        this.imageStorageService = imageStorageService;
        this.medicineExportService = medicineExportService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<MedicineResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false, name = "category") UUID category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                medicineService.list(search, categoryId != null ? categoryId : category, minPrice, maxPrice, page,
                        size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(medicineService.getById(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<MedicineResponse> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(medicineService.getBySku(sku));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countByCategory(@RequestParam UUID categoryId) {
        return ResponseEntity.ok(medicineService.countByCategoryId(categoryId));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(required = false) UUID categoryId) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=medicines.xlsx")
                .body(medicineExportService.exportExcel(categoryId));
    }

    @PostMapping
    public ResponseEntity<MedicineResponse> create(@Valid @RequestBody CreateMedicineRequest request) {
        return ResponseEntity.ok(medicineService.create(request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicineResponse> createMultipart(
            @RequestPart("payload") @Valid CreateMedicineRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(medicineService.create(request, image));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicineResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateMedicineRequest request) {
        return ResponseEntity.ok(medicineService.update(id, request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicineResponse> updateMultipart(@PathVariable UUID id,
            @RequestPart("payload") @Valid UpdateMedicineRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(medicineService.update(id, request, image));
    }

    @PostMapping(value = "/{id}/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<MedicineResponse> uploadImage(@PathVariable UUID id,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        return ResponseEntity.ok(medicineService.updateImage(id, image,
                body != null ? body.get("imageUrl") : null));
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable UUID id) {
        MedicineResponse medicine = medicineService.getById(id);
        return imageStorageService.loadAsResponseEntity(medicine.imageUrl());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        medicineService.softDelete(id);
        return ResponseEntity.ok().build();
    }
}
