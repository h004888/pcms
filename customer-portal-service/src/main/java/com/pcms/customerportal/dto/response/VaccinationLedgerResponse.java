package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.VaccinationLedger;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UC14 - VACCINE-LEDGER. Permanent vaccination history row (B2C view).
 */
public record VaccinationLedgerResponse(
        UUID id,
        UUID customerId,
        UUID familyMemberId,
        UUID vaccineId,
        Integer doseNumber,
        LocalDateTime administeredAt,
        String batchNo,
        UUID branchId,
        String notes,
        LocalDateTime createdAt
) {
    public static VaccinationLedgerResponse from(VaccinationLedger v) {
        return new VaccinationLedgerResponse(
                v.getId(),
                v.getCustomerId(),
                v.getFamilyMemberId(),
                v.getVaccineId(),
                v.getDoseNumber(),
                v.getAdministeredAt(),
                v.getBatchNo(),
                v.getBranchId(),
                v.getNotes(),
                v.getCreatedAt()
        );
    }
}