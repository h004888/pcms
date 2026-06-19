package com.pcms.mobilebff.controller;

import com.pcms.mobilebff.dto.request.CreateReminderRequest;
import com.pcms.mobilebff.dto.response.ReminderResponse;
import com.pcms.mobilebff.service.ReminderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mobile/medication-reminders")
@Tag(name = "UC17 - Mobile Medication Reminder (MOBILE-MED-REMINDER)")
public class ReminderController {

    private final ReminderService service;

    public ReminderController(ReminderService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a new medication reminder (5-step onboarding)")
    public ResponseEntity<ReminderResponse> create(@Valid @RequestBody CreateReminderRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @GetMapping
    @Operation(summary = "List active reminders for a customer")
    public ResponseEntity<List<ReminderResponse>> list(@RequestParam UUID customerId) {
        return ResponseEntity.ok(service.listByCustomer(customerId));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a reminder (soft-disable)")
    public ResponseEntity<ReminderResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(service.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a reminder permanently")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
