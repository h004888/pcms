package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.VaccineBookingSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VaccineBookingSequenceRepository extends JpaRepository<VaccineBookingSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM VaccineBookingSequence s WHERE s.datePrefix = :prefix")
    Optional<VaccineBookingSequence> findByIdForUpdate(@Param("prefix") String datePrefix);
}