package com.pcms.prescriptionservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PrescriptionItemRequest(
        @NotNull(message = "medicineId is required")
        UUID medicineId,
        String dosage,
        @Min(value = 1, message = "durationDays must be >= 1")
        Integer durationDays
) {
}
