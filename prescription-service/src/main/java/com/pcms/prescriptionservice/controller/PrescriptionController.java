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
                request.licenseNo());
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

    /**
     * TICKET-301: POST alias for sign - aligns with SDD §6.14 (POST /sign).
     * Some HTTP clients/sdks generate POST by default; keeping both avoids
     * 405 errors.
     */
    @PostMapping("/{id}/sign")
    public ResponseEntity<PrescriptionResponse> signPost(@PathVariable UUID id) {
        return ResponseEntity.ok(prescriptionService.sign(id));
    }

    /** POST /api/v1/prescriptions/{id}/link-order?orderId=... - B-16 */
    @PostMapping("/{id}/link-order")
    public ResponseEntity<PrescriptionResponse> linkOrder(@PathVariable UUID id,
            @RequestParam UUID orderId) {
        return ResponseEntity.ok(prescriptionService.linkOrder(id, orderId));
    }

    @GetMapping("/{id}/print")
    public ResponseEntity<PrescriptionResponse> print(@PathVariable UUID id) {
        return ResponseEntity.ok(prescriptionService.print(id));
    }

    /**
     * TICKET-302: POST alias for print - aligns with SDD §6.14 (POST /print).
     * Frontend sometimes triggers print via POST when generating a downloadable
     * artifact.
     */
    @PostMapping("/{id}/print")
    public ResponseEntity<PrescriptionResponse> printPost(@PathVariable UUID id) {
        return ResponseEntity.ok(prescriptionService.print(id));
    }

    /**
     * TICKET-303: DELETE /prescriptions/{id} - cancel a prescription.
     * Allowed only when:
     *   - status = DRAFT (anytime), OR
     *   - status = SIGNED and {@code orderId} is null (i.e. not yet linked
     *     to a paid order).
     * Throws {@code BusinessException} (409) if the prescription is linked
     * to an order.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<PrescriptionResponse> cancel(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID actorHeader) {
        UUID actor = userId != null ? userId : actorHeader;
        return ResponseEntity.ok(prescriptionService.cancel(id, actor));
    }
}
