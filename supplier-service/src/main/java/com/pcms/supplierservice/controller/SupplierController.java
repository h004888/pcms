package com.pcms.supplierservice.controller;

import com.pcms.supplierservice.entity.Supplier;
import com.pcms.supplierservice.enums.SupplierStatus;
import com.pcms.supplierservice.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierRepository supplierRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("name"));
        Page<Supplier> suppliers = supplierRepository.search(search, pageable);
        return ResponseEntity.ok(Map.of(
            "data", suppliers.getContent(),
            "page", suppliers.getNumber(),
            "size", suppliers.getSize(),
            "total", suppliers.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getById(@PathVariable UUID id) {
        return supplierRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Supplier supplier) {
        if (supplierRepository.existsByTaxCode(supplier.getTaxCode())) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG33", "message", "Tax code already exists"));
        }
        if (supplier.getStatus() == null) supplier.setStatus(SupplierStatus.ACTIVE);
        return ResponseEntity.ok(supplierRepository.save(supplier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> update(@PathVariable UUID id, @RequestBody Supplier details) {
        Optional<Supplier> optional = supplierRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Supplier s = optional.get();
        s.setName(details.getName());
        s.setContactPerson(details.getContactPerson());
        s.setPhone(details.getPhone());
        s.setEmail(details.getEmail());
        s.setAddress(details.getAddress());
        s.setBankName(details.getBankName());
        s.setBankAccount(details.getBankAccount());
        s.setStatus(details.getStatus());
        return ResponseEntity.ok(supplierRepository.save(s));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable UUID id) {
        Optional<Supplier> optional = supplierRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Supplier s = optional.get();
        s.setStatus(SupplierStatus.INACTIVE);
        supplierRepository.save(s);
        return ResponseEntity.ok().build();
    }
}
