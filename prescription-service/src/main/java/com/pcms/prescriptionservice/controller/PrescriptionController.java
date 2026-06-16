package com.pcms.prescriptionservice.controller;

import com.pcms.prescriptionservice.dto.CreatePrescriptionRequest;
import com.pcms.common.dto.PageResponse;
import com.pcms.prescriptionservice.dto.PrescriptionResponse;
import com.pcms.prescriptionservice.dto.UpdatePrescriptionRequest;
import com.pcms.prescriptionservice.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * UC12 - Issue Prescription
 * FR12.1, FR12.2, FR12.3, FR12.4
 */
@RestController
@RequestMapping("/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<PrescriptionResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(prescriptionService.list(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(prescriptionService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<PrescriptionResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(prescriptionService.getByCode(code));
    }

    /**
     * POST /api/v1/prescriptions - Step 4-9 of UC12 main flow
     * Auto-generates code RX-yyyy####
     */
    @PostMapping
    public ResponseEntity<PrescriptionResponse> create(@Valid @RequestBody CreatePrescriptionRequest request) {
        return ResponseEntity.ok(prescriptionService.create(request));
    }

    /** AT1: Save as draft */
    @PostMapping("/draft")
    public ResponseEntity<PrescriptionResponse> saveDraft(@Valid @RequestBody CreatePrescriptionRequest request) {
        CreatePrescriptionRequest asDraft = new CreatePrescriptionRequest(
                request.patientId(),
                request.doctorId(),
                request.diagnosis(),
                request.notes(),
                request.items(),
                Boolean.TRUE,
                request.licenseNo()
        );
        return ResponseEntity.ok(prescriptionService.create(asDraft));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> update(@PathVariable UUID id,
                                                       @Valid @RequestBody UpdatePrescriptionRequest request) {
        return ResponseEntity.ok(prescriptionService.update(id, request));
    }

    @PutMapping("/{id}/sign")
    public ResponseEntity<PrescriptionResponse> sign(@PathVariable UUID id) {
        return ResponseEntity.ok(prescriptionService.sign(id));
    }

    @GetMapping("/{id}/print")
    public ResponseEntity<PrescriptionResponse> print(@PathVariable UUID id) {
        return ResponseEntity.ok(prescriptionService.print(id));
    }
}
