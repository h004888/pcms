package com.pcms.customerportal.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.dto.request.CreateVaccineBookingRequest;
import com.pcms.customerportal.dto.response.VaccinationLedgerResponse;
import com.pcms.customerportal.dto.response.VaccineBookingResponse;
import com.pcms.customerportal.dto.response.VaccineResponse;
import com.pcms.customerportal.dto.response.VaccineSlotResponse;
import com.pcms.customerportal.entity.Vaccine;
import com.pcms.customerportal.entity.VaccineBooking;
import com.pcms.customerportal.entity.VaccineBookingSequence;
import com.pcms.customerportal.entity.VaccineSlot;
import com.pcms.customerportal.entity.VaccinationLedger;
import com.pcms.customerportal.enums.VaccineBookingStatus;
import com.pcms.customerportal.repository.VaccinationLedgerRepository;
import com.pcms.customerportal.repository.VaccineBookingRepository;
import com.pcms.customerportal.repository.VaccineBookingSequenceRepository;
import com.pcms.customerportal.repository.VaccineRepository;
import com.pcms.customerportal.repository.VaccineSlotRepository;
import com.pcms.customerportal.service.VaccineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Vaccine booking service implementation.
 *
 * <p>Atomic slot decrement uses {@code PESSIMISTIC_WRITE} on the
 * {@code vaccine_slots} row. Booking code generation uses
 * {@code PESSIMISTIC_WRITE} on the sequence row (mirrors B-08).
 */
@Service
public class VaccineServiceImpl implements VaccineService {

    private static final Logger log = LoggerFactory.getLogger(VaccineServiceImpl.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final VaccineRepository vaccineRepository;
    private final VaccineSlotRepository slotRepository;
    private final VaccineBookingRepository bookingRepository;
    private final VaccineBookingSequenceRepository sequenceRepository;
    private final VaccinationLedgerRepository ledgerRepository;

    public VaccineServiceImpl(VaccineRepository vaccineRepository,
                              VaccineSlotRepository slotRepository,
                              VaccineBookingRepository bookingRepository,
                              VaccineBookingSequenceRepository sequenceRepository,
                              VaccinationLedgerRepository ledgerRepository) {
        this.vaccineRepository = vaccineRepository;
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.sequenceRepository = sequenceRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VaccineResponse> listVaccines(String manufacturer, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        Page<Vaccine> result = (manufacturer == null || manufacturer.isBlank())
                ? vaccineRepository.findByStatusOrderByNameAsc("ACTIVE", pageable)
                : vaccineRepository.findByStatusAndManufacturerContainingIgnoreCaseOrderByNameAsc(
                        "ACTIVE", manufacturer.trim(), pageable);
        return PageResponse.of(result, VaccineResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaccineSlotResponse> listAvailableSlots(UUID vaccineId, UUID branchId, LocalDate date) {
        if (vaccineId == null) {
            throw new InvalidOperationException("vaccineId is required", "Thiếu vaccineId");
        }
        LocalDate fromDate = date != null ? date : LocalDate.now();
        List<VaccineSlot> slots = slotRepository.findAvailable(vaccineId, branchId, fromDate);
        return slots.stream().map(VaccineSlotResponse::from).toList();
    }

    @Override
    @Transactional
    public VaccineBookingResponse createBooking(UUID customerId, CreateVaccineBookingRequest request) {
        if (customerId == null) {
            throw new InvalidOperationException(
                    "customerId is required",
                    "Thiếu thông tin khách hàng hiện tại");
        }

        // PESSIMISTIC_WRITE lock on the slot row for atomic decrement
        VaccineSlot slot = slotRepository.lockById(request.slotId())
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine slot", request.slotId()));

        if (!slot.getVaccineId().equals(request.vaccineId())) {
            throw new InvalidOperationException(
                    "slot does not belong to the requested vaccine",
                    "Khung giờ không thuộc vắc xin đã chọn");
        }
        if (slot.getSlotDate().isBefore(LocalDate.now())) {
            throw new InvalidOperationException(
                    "cannot book a slot in the past",
                    "Không thể đặt lịch cho khung giờ đã qua");
        }
        if (slot.getAvailableQty() <= 0) {
            throw new InvalidOperationException(
                    "slot is full",
                    "Khung giờ đã hết chỗ");
        }

        Vaccine vaccine = vaccineRepository.findById(request.vaccineId())
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine", request.vaccineId()));
        if (!"ACTIVE".equals(vaccine.getStatus())) {
            throw new InvalidOperationException(
                    "vaccine is not active",
                    "Vắc xin hiện không khả dụng");
        }

        // Atomic decrement
        slot.setAvailableQty(slot.getAvailableQty() - 1);
        slotRepository.save(slot);

        // Generate booking code (mirrors OrderServiceImpl.generateOrderNumber)
        String bookingCode = generateBookingCode();

        VaccineBooking booking = new VaccineBooking();
        booking.setBookingCode(bookingCode);
        booking.setCustomerId(customerId);
        booking.setVaccineId(request.vaccineId());
        booking.setSlotId(request.slotId());
        booking.setFamilyMemberId(request.familyMemberId());
        booking.setStatus(VaccineBookingStatus.BOOKED);

        booking = bookingRepository.save(booking);
        log.info("[VaccineBooking] created booking code={} customer={} vaccine={} slot={}",
                bookingCode, customerId, request.vaccineId(), request.slotId());

        return VaccineBookingResponse.from(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VaccineBookingResponse> listMyBookings(UUID customerId,
                                                               VaccineBookingStatus status,
                                                               int page, int size) {
        if (customerId == null) {
            throw new InvalidOperationException(
                    "customerId is required",
                    "Thiếu thông tin khách hàng hiện tại");
        }
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        Page<VaccineBooking> result = (status == null)
                ? bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                : bookingRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status, pageable);
        return PageResponse.of(result, VaccineBookingResponse::from);
    }

    @Override
    @Transactional
    public VaccineBookingResponse cancelBooking(UUID customerId, UUID bookingId, String reason) {
        if (customerId == null) {
            throw new InvalidOperationException(
                    "customerId is required",
                    "Thiếu thông tin khách hàng hiện tại");
        }
        VaccineBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine booking", bookingId));

        if (!booking.getCustomerId().equals(customerId)) {
            throw new InvalidOperationException(
                    "cannot cancel another customer's booking",
                    "Không thể huỷ đặt lịch của khách hàng khác");
        }
        if (booking.getStatus() != VaccineBookingStatus.BOOKED) {
            throw new InvalidOperationException(
                    "only BOOKED bookings can be cancelled (current=" + booking.getStatus() + ")",
                    "Chỉ có thể huỷ đặt lịch đang ở trạng thái BOOKED");
        }

        // Reload slot to restore available qty atomically
        VaccineSlot slot = slotRepository.lockById(booking.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine slot", booking.getSlotId()));
        if (slot.getSlotDate().isBefore(LocalDate.now())
                || slot.getSlotDate().isEqual(LocalDate.now())) {
            throw new InvalidOperationException(
                    "cannot cancel a booking for today or past slots",
                    "Không thể huỷ đặt lịch cho khung giờ trong ngày hoặc đã qua");
        }
        slot.setAvailableQty(slot.getAvailableQty() + 1);
        slotRepository.save(slot);

        booking.setStatus(VaccineBookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelReason(reason);
        booking = bookingRepository.save(booking);

        log.info("[VaccineBooking] cancelled booking code={} reason={}", booking.getBookingCode(), reason);
        return VaccineBookingResponse.from(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VaccinationLedgerResponse> myLedger(UUID customerId, UUID memberId,
                                                            int page, int size) {
        if (customerId == null) {
            throw new InvalidOperationException(
                    "customerId is required",
                    "Thiếu thông tin khách hàng hiện tại");
        }
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        Page<VaccinationLedger> result = ledgerRepository.findHistoryByCustomer(customerId, memberId, pageable);
        return PageResponse.of(result, VaccinationLedgerResponse::from);
    }

    /**
     * Generate booking code VC-yyyyMMdd-#### using a per-date sequence row
     * locked with PESSIMISTIC_WRITE (mirrors order-service B-08).
     */
    private String generateBookingCode() {
        String datePrefix = LocalDate.now().format(DATE_FMT);
        Optional<VaccineBookingSequence> seqOpt = sequenceRepository.findByIdForUpdate(datePrefix);
        VaccineBookingSequence seq;
        if (seqOpt.isPresent()) {
            seq = seqOpt.get();
            seq.setLastSeq(seq.getLastSeq() + 1);
        } else {
            seq = new VaccineBookingSequence(datePrefix, 1);
        }
        sequenceRepository.save(seq);
        return String.format("VC-%s-%04d", datePrefix, seq.getLastSeq());
    }
}