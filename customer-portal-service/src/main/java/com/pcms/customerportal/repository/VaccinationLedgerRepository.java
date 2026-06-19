package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.VaccinationLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VaccinationLedgerRepository extends JpaRepository<VaccinationLedger, UUID> {

    /** Customer's full vaccination history across all family members. */
    @Query("""
            SELECT v FROM VaccinationLedger v
            WHERE v.customerId = :customerId
              AND (:memberId IS NULL OR v.familyMemberId = :memberId OR
                   (:memberId IS NOT NULL AND v.familyMemberId IS NULL AND v.customerId = :memberId))
            ORDER BY v.administeredAt DESC
            """)
    Page<VaccinationLedger> findHistoryByCustomer(@Param("customerId") UUID customerId,
                                                  @Param("memberId") UUID memberId,
                                                  Pageable pageable);
}