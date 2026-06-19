package com.pcms.customerportal.controller;

import com.pcms.common.dto.PageResponse;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.customerportal.dto.request.CreateVaccineBookingRequest;
import com.pcms.customerportal.dto.response.VaccinationLedgerResponse;
import com.pcms.customerportal.dto.response.VaccineBookingResponse;
import com.pcms.customerportal.dto.response.VaccineResponse;
import com.pcms.customerportal.dto.response.VaccineSlotResponse;
import com.pcms.customerportal.enums.VaccineBookingStatus;
import com.pcms.customerportal.service.VaccineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * UC14 - Vaccine booking (VACCINE-HOME, VACCINE-BOOKING, VACCINE-LEDGER).
 *
 * <p>Customer is identified via the {@code X-Customer-Id} header forwarded
 * by the API Gateway (populated from JWT.sub for the B2C portal).
 */
@RestController
@Tag(name = "UC14 - Customer Portal / Vaccine")
public class VaccineController {

    private final VaccineService vaccineService;

    public VaccineController(VaccineService vaccineService) {
        this.vaccineService = vaccineService;
    }

    @GetMapping("/vaccines")
    @Operation(summary = "VACCINE-HOME - list active vaccines")
    public ResponseEntity<PageResponse<VaccineResponse>> listVaccines(
            @RequestParam(name = "manufacturer", required = false) String manufacturer,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(vaccineService.listVaccines(manufacturer, page, size));
    }

    @GetMapping("/vaccines/{id}/slots")
    @Operation(summary = "VACCINE-BOOKING - list available slots for a vaccine")
    public ResponseEntity<List<VaccineSlotResponse>> listSlots(
            @PathVariable("id") UUID vaccineId,
            @RequestParam(name = "branchId", required = false) UUID branchId,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(vaccineService.listAvailableSlots(vaccineId, branchId, date));
    }

    @PostMapping("/vaccine-bookings")
    @Operation(summary = "VACCINE-BOOKING - create a vaccine appointment")
    public ResponseEntity<VaccineBookingResponse> createBooking(
            @Parameter(in = ParameterIn.HEADER, name = "X-Customer-Id",
                    description = "Customer UUID forwarded by API Gateway from JWT.sub")
            @RequestHeader(value = "X-Customer-Id", required = false) UUID customerId,
            @Valid @RequestBody CreateVaccineBookingRequest request) {
        UUID resolved = resolveCustomerId(customerId, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vaccineService.createBooking(resolved, request));
    }

    @GetMapping("/vaccine-bookings/me")
    @Operation(summary = "VACCINE-BOOKING - list my bookings")
    public ResponseEntity<PageResponse<VaccineBookingResponse>> myBookings(
            @RequestHeader(value = "X-Customer-Id", required = false) UUID customerId,
            @RequestParam(name = "customerId", required = false) UUID fallbackCustomerId,
            @RequestParam(name = "status", required = false) VaccineBookingStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        UUID resolved = resolveCustomerId(customerId, fallbackCustomerId);
        return ResponseEntity.ok(vaccineService.listMyBookings(resolved, status, page, size));
    }

    @DeleteMapping("/vaccine-bookings/{id}")
    @Operation(summary = "VACCINE-BOOKING - cancel a booking, restores the slot")
    public ResponseEntity<VaccineBookingResponse> cancelBooking(
            @PathVariable("id") UUID bookingId,
            @RequestHeader(value = "X-Customer-Id", required = false) UUID customerId,
            @RequestParam(name = "customerId", required = false) UUID fallbackCustomerId,
            @RequestParam(name = "reason", required = false) String reason) {
        UUID resolved = resolveCustomerId(customerId, fallbackCustomerId);
        return ResponseEntity.ok(vaccineService.cancelBooking(resolved, bookingId, reason));
    }

    @GetMapping("/vaccination-ledger/me")
    @Operation(summary = "VACCINE-LEDGER - my full vaccination history")
    public ResponseEntity<PageResponse<VaccinationLedgerResponse>> myLedger(
            @RequestHeader(value = "X-Customer-Id", required = false) UUID customerId,
            @RequestParam(name = "customerId", required = false) UUID fallbackCustomerId,
            @RequestParam(name = "memberId", required = false) UUID memberId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        UUID resolved = resolveCustomerId(customerId, fallbackCustomerId);
        return ResponseEntity.ok(vaccineService.myLedger(resolved, memberId, page, size));
    }

    private UUID resolveCustomerId(UUID headerId, UUID fallbackId) {
        if (headerId != null) {
            return headerId;
        }
        if (fallbackId != null) {
            return fallbackId;
        }
        throw new InvalidOperationException(
                "customerId is required (X-Customer-Id header or customerId query param)",
                "Thiếu thông tin định danh khách hàng (header X-Customer-Id hoặc customerId)");
    }
}