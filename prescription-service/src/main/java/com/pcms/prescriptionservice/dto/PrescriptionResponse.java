package com.pcms.prescriptionservice.dto;

import com.pcms.prescriptionservice.enums.PrescriptionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PrescriptionResponse(
                UUID id,
                String code,
                UUID patientId,
                UUID doctorId,
                UUID orderId,
                String diagnosis,
                String notes,
                String signatureHash,
                PrescriptionStatus status,
                LocalDateTime issuedAt,
                List<PrescriptionItemRequest> items) {
}
