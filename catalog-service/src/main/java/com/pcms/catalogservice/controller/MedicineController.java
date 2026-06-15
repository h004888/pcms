package com.pcms.catalogservice.controller;

import com.pcms.catalogservice.entity.Medicine;
import com.pcms.catalogservice.enums.MedicineStatus;
import com.pcms.catalogservice.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * UC04 - Manage Medicines
 * UC10 - Search Medicines (SCR-SEARCH, FR10.1-FR10.4)
 */
@RestController
@RequestMapping("/medicines")
public class MedicineController {

    @Autowired
    private MedicineRepository medicineRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("name").ascending());
        Page<Medicine> medicines = medicineRepository.search(search, categoryId, minPrice, maxPrice, MedicineStatus.ACTIVE, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", medicines.getContent());
        response.put("page", medicines.getNumber());
        response.put("size", medicines.getSize());
        response.put("total", medicines.getTotalElements());
        response.put("totalPages", medicines.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getById(@PathVariable UUID id) {
        return medicineRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<Medicine> getBySku(@PathVariable String sku) {
        return medicineRepository.findBySku(sku)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Medicine medicine) {
        if (medicineRepository.existsBySku(medicine.getSku())) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG33", "message", "SKU already exists"));
        }
        if (medicine.getStatus() == null) medicine.setStatus(MedicineStatus.ACTIVE);
        if (medicine.getPrescriptionRequired() == null) medicine.setPrescriptionRequired(false);
        return ResponseEntity.ok(medicineRepository.save(medicine));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medicine> update(@PathVariable UUID id, @RequestBody Medicine details) {
        Optional<Medicine> optional = medicineRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Medicine medicine = optional.get();
        medicine.setName(details.getName());
        medicine.setPrice(details.getPrice());
        medicine.setUnit(details.getUnit());
        medicine.setCategoryId(details.getCategoryId());
        medicine.setSupplierId(details.getSupplierId());
        medicine.setPrescriptionRequired(details.getPrescriptionRequired());
        medicine.setImageUrl(details.getImageUrl());
        medicine.setStatus(details.getStatus());
        return ResponseEntity.ok(medicineRepository.save(medicine));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable UUID id) {
        Optional<Medicine> optional = medicineRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Medicine medicine = optional.get();
        medicine.setStatus(MedicineStatus.INACTIVE);
        medicineRepository.save(medicine);
        return ResponseEntity.ok().build();
    }
}
