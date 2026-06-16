package com.pcms.prescriptionservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreatePrescriptionRequest(
        @NotNull(message = "patientId is required")
        UUID patientId,
        @NotNull(message = "doctorId is required")
        UUID doctorId,
        @NotBlank(message = "diagnosis is required")
        String diagnosis,
        String notes,
        @NotEmpty(message = "At least one item is required")
        @Valid
        List<PrescriptionItemRequest> items,
        Boolean saveAsDraft,
        String licenseNo
) {
}
