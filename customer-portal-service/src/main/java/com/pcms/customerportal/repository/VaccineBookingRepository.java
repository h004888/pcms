package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.VaccineBooking;
import com.pcms.customerportal.enums.VaccineBookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VaccineBookingRepository extends JpaRepository<VaccineBooking, UUID> {

    Optional<VaccineBooking> findByBookingCode(String bookingCode);

    Page<VaccineBooking> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    Page<VaccineBooking> findByCustomerIdAndStatusOrderByCreatedAtDesc(
            UUID customerId, VaccineBookingStatus status, Pageable pageable);
}