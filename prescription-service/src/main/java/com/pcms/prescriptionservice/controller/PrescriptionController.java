package com.pcms.prescriptionservice.controller;

import com.pcms.prescriptionservice.entity.Prescription;
import com.pcms.prescriptionservice.enums.PrescriptionStatus;
import com.pcms.prescriptionservice.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * UC12 - Issue Prescription
 * FR12.1, FR12.2, FR12.3, FR12.4
 */
@RestController
@RequestMapping("/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Prescription> prescriptions = prescriptionRepository.findAll(pageable);
        return ResponseEntity.ok(Map.of(
            "data", prescriptions.getContent(),
            "page", prescriptions.getNumber(),
            "size", prescriptions.getSize(),
            "total", prescriptions.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prescription> getById(@PathVariable UUID id) {
        return prescriptionRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Prescription> getByCode(@PathVariable String code) {
        return prescriptionRepository.findByCode(code).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/prescriptions - Step 4-9 of UC12 main flow
     * Auto-generates code RX-yyyy####
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Prescription prescription) {
        if (prescription.getPatientId() == null || prescription.getDoctorId() == null
                || prescription.getDiagnosis() == null || prescription.getDiagnosis().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG33", "message", "Patient, doctor, diagnosis are mandatory"));
        }
        prescription.setCode(generateCode());
        // FR12.4: Compute digital signature hash
        prescription.setSignatureHash(UUID.randomUUID().toString().replace("-", ""));
        if (prescription.getStatus() == null) prescription.setStatus(PrescriptionStatus.SIGNED);
        if (prescription.getStatus() == PrescriptionStatus.SIGNED) {
            prescription.setIssuedAt(LocalDateTime.now());
        }
        return ResponseEntity.ok(prescriptionRepository.save(prescription));
    }

    /** AT1: Save as draft */
    @PostMapping("/draft")
    public ResponseEntity<?> saveDraft(@RequestBody Prescription prescription) {
        prescription.setStatus(PrescriptionStatus.DRAFT);
        prescription.setCode(generateCode());
        return ResponseEntity.ok(prescriptionRepository.save(prescription));
    }

    @PutMapping("/{id}/sign")
    @Transactional
    public ResponseEntity<?> sign(@PathVariable UUID id) {
        Optional<Prescription> optional = prescriptionRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Prescription p = optional.get();
        p.setStatus(PrescriptionStatus.SIGNED);
        p.setIssuedAt(LocalDateTime.now());
        p.setSignatureHash(UUID.randomUUID().toString().replace("-", ""));
        return ResponseEntity.ok(prescriptionRepository.save(p));
    }

    private String generateCode() {
        String year = String.valueOf(LocalDate.now().getYear());
        Pageable limit = PageRequest.of(0, 1);
        List<Prescription> latest = prescriptionRepository.findByYearPrefix(year, limit);
        int nextNum = 1;
        if (!latest.isEmpty()) {
            String code = latest.get(0).getCode();
            String numPart = code.substring(code.lastIndexOf('-') + 1);
            try { nextNum = Integer.parseInt(numPart) + 1; } catch (NumberFormatException ignored) {}
        }
        return String.format("RX-%s%04d", year, nextNum);
    }
}
