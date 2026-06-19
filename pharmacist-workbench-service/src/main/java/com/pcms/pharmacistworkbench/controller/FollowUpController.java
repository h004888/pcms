package com.pcms.pharmacistworkbench.controller;

import com.pcms.pharmacistworkbench.dto.request.ScheduleFollowUpRequest;
import com.pcms.pharmacistworkbench.dto.response.FollowUpTaskResponse;
import com.pcms.pharmacistworkbench.service.FollowUpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/follow-ups")
@Tag(name = "UC16 - Follow-up Tasks (RX-FOLLOW-UP)")
public class FollowUpController {

    private final FollowUpService service;

    public FollowUpController(FollowUpService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Schedule a follow-up task (3/7/14-day after purchase)")
    public ResponseEntity<FollowUpTaskResponse> schedule(@Valid @RequestBody ScheduleFollowUpRequest request) {
        return ResponseEntity.ok(service.schedule(request));
    }

    @GetMapping("/by-customer/{customerId}")
    @Operation(summary = "List follow-ups for a customer")
    public ResponseEntity<List<FollowUpTaskResponse>> listByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(service.listByCustomer(customerId));
    }

    @PostMapping("/{id}/response")
    @Operation(summary = "Record customer response (TAKEN_OK/SIDE_EFFECT/NO_EFFECT)")
    public ResponseEntity<FollowUpTaskResponse> recordResponse(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.recordResponse(id, body.get("response"), body.get("note")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a follow-up task")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
