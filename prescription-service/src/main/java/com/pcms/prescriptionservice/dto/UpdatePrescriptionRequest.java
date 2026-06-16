package com.pcms.prescriptionservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdatePrescriptionRequest(
        @NotBlank(message = "diagnosis is required")
        String diagnosis,
        String notes,
        @NotEmpty(message = "At least one item is required")
        @Valid
        List<PrescriptionItemRequest> items
) {
}
