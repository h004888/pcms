package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.VaccineSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VaccineSlotRepository extends JpaRepository<VaccineSlot, UUID> {

    /**
     * Lock the slot row for atomic decrement/restore (B-08 equivalent).
     * Translates to {@code SELECT ... FOR UPDATE} in MySQL.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM VaccineSlot s WHERE s.id = :id")
    Optional<VaccineSlot> lockById(@Param("id") UUID id);

    /** Available slots for a given vaccine + branch, filtered by date >= today. */
    @Query("""
            SELECT s FROM VaccineSlot s
            WHERE s.vaccineId = :vaccineId
              AND s.availableQty > 0
              AND s.slotDate >= :fromDate
              AND (:branchId IS NULL OR s.branchId = :branchId)
            ORDER BY s.slotDate, s.slotTime
            """)
    List<VaccineSlot> findAvailable(@Param("vaccineId") UUID vaccineId,
                                    @Param("branchId") UUID branchId,
                                    @Param("fromDate") LocalDate fromDate);

    /** Slots for a specific date (regardless of availability) - for slot detail page. */
    List<VaccineSlot> findByVaccineIdAndSlotDateOrderBySlotTimeAsc(UUID vaccineId, LocalDate slotDate);
}