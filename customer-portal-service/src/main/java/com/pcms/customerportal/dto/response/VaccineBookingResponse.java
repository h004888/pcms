package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.VaccineBooking;
import com.pcms.customerportal.enums.VaccineBookingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UC14 - VACCINE-LEDGER. Booking confirmation / list item.
 */
public record VaccineBookingResponse(
        UUID id,
        String bookingCode,
        UUID customerId,
        UUID vaccineId,
        UUID slotId,
        UUID familyMemberId,
        VaccineBookingStatus status,
        LocalDateTime cancelledAt,
        String cancelReason,
        LocalDateTime createdAt
) {
    public static VaccineBookingResponse from(VaccineBooking b) {
        return new VaccineBookingResponse(
                b.getId(),
                b.getBookingCode(),
                b.getCustomerId(),
                b.getVaccineId(),
                b.getSlotId(),
                b.getFamilyMemberId(),
                b.getStatus(),
                b.getCancelledAt(),
                b.getCancelReason(),
                b.getCreatedAt()
        );
    }
}