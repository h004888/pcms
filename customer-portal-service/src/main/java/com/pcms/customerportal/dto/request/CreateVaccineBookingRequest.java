package com.pcms.customerportal.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * UC14 - VACCINE-BOOKING. Request body to create a vaccine appointment.
 *
 * <p>Atomic slot decrement is enforced at the service layer via
 * {@code PESSIMISTIC_WRITE} lock on the vaccine_slot row.
 */
public record CreateVaccineBookingRequest(

        @NotNull(message = "vaccineId is required")
        UUID vaccineId,

        @NotNull(message = "slotId is required")
        UUID slotId,

        /** Optional - if absent, the booking is for the customer themselves. */
        UUID familyMemberId,

        @Size(max = 255, message = "note must not exceed 255 characters")
        String note
) {
}