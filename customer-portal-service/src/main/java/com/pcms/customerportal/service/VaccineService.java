package com.pcms.customerportal.service;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.request.CreateVaccineBookingRequest;
import com.pcms.customerportal.dto.response.VaccinationLedgerResponse;
import com.pcms.customerportal.dto.response.VaccineBookingResponse;
import com.pcms.customerportal.dto.response.VaccineResponse;
import com.pcms.customerportal.dto.response.VaccineSlotResponse;
import com.pcms.customerportal.enums.VaccineBookingStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * UC14 - VACCINE booking service.
 *
 * <p>Atomic slot decrement is enforced at the implementation layer via
 * {@code @Transactional} + PESSIMISTIC_WRITE lock on the slot row.
 */
public interface VaccineService {

    /** List active vaccines, optionally filtered by manufacturer. */
    PageResponse<VaccineResponse> listVaccines(String manufacturer, int page, int size);

    /** List available slots for a vaccine, filtered by branch + date. */
    List<VaccineSlotResponse> listAvailableSlots(UUID vaccineId, UUID branchId, LocalDate date);

    /** Create a booking for the given customer. Atomically reserves 1 slot. */
    VaccineBookingResponse createBooking(UUID customerId, CreateVaccineBookingRequest request);

    /** List customer's bookings (most recent first). */
    PageResponse<VaccineBookingResponse> listMyBookings(UUID customerId,
                                                       VaccineBookingStatus status,
                                                       int page, int size);

    /** Cancel a booking and restore the slot. Only allowed before slot date. */
    VaccineBookingResponse cancelBooking(UUID customerId, UUID bookingId, String reason);

    /** Customer's full vaccination history (optionally filtered to one member). */
    PageResponse<VaccinationLedgerResponse> myLedger(UUID customerId, UUID memberId,
                                                     int page, int size);
}