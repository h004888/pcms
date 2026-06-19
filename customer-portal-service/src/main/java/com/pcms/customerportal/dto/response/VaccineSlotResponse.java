package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.VaccineSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * UC14 - VACCINE-BOOKING. Bookable slot summary.
 */
public record VaccineSlotResponse(
        UUID id,
        UUID vaccineId,
        UUID branchId,
        LocalDate slotDate,
        LocalTime slotTime,
        Integer availableQty,
        Integer totalQty,
        LocalDateTime createdAt
) {
    public static VaccineSlotResponse from(VaccineSlot s) {
        return new VaccineSlotResponse(
                s.getId(),
                s.getVaccineId(),
                s.getBranchId(),
                s.getSlotDate(),
                s.getSlotTime(),
                s.getAvailableQty(),
                s.getTotalQty(),
                s.getCreatedAt()
        );
    }
}