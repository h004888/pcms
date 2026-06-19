package com.pcms.pharmacistworkbench.controller;

import com.pcms.pharmacistworkbench.dto.request.StartConsultationRequest;
import com.pcms.pharmacistworkbench.dto.response.ConsultationResponse;
import com.pcms.pharmacistworkbench.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/consultations")
@Tag(name = "UC16 - Pharmacist Consultation (RX-CONSULT)")
public class ConsultationController {

    private final ConsultationService service;

    public ConsultationController(ConsultationService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Start a new consultation session (text/voice/video)")
    public ResponseEntity<ConsultationResponse> start(
            @Valid @RequestBody StartConsultationRequest request,
            @RequestHeader("X-User-Id") UUID pharmacistId) {
        return ResponseEntity.ok(service.start(request, pharmacistId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get consultation by id")
    public ResponseEntity<ConsultationResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping("/{id}/end")
    @Operation(summary = "End a consultation session")
    public ResponseEntity<ConsultationResponse> end(@PathVariable UUID id) {
        return ResponseEntity.ok(service.end(id));
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Append message to consultation transcript")
    public ResponseEntity<ConsultationResponse> appendMessage(
            @PathVariable UUID id,
            @RequestBody String message) {
        return ResponseEntity.ok(service.appendMessage(id, message));
    }

    @GetMapping("/by-customer/{customerId}")
    @Operation(summary = "List consultations for a customer")
    public ResponseEntity<List<ConsultationResponse>> listByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(service.listByCustomer(customerId));
    }

    @GetMapping("/by-pharmacist/{pharmacistId}")
    @Operation(summary = "List active consultations for a pharmacist")
    public ResponseEntity<List<ConsultationResponse>> listByPharmacist(@PathVariable UUID pharmacistId) {
        return ResponseEntity.ok(service.listByPharmacist(pharmacistId));
    }
}
