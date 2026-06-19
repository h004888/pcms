package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.CustomerAddress;
import com.pcms.customerportal.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {

    /** List all active addresses of a customer (used by GET /addresses). */
    List<CustomerAddress> findByCustomerIdAndStatusOrderByIsDefaultDescCreatedAtDesc(
            UUID customerId, RecordStatus status);

    /** Look up by id - ownership check happens in service layer. */
    Optional<CustomerAddress> findByIdAndStatus(UUID id, RecordStatus status);

    /**
     * Atomic "unset all defaults" used by the transactional setDefault flow.
     * Returns number of rows updated.
     */
    @Modifying
    @Query("UPDATE CustomerAddress a SET a.isDefault = false, a.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE a.customerId = :customerId AND a.isDefault = true AND a.id <> :excludeId")
    int clearOtherDefaults(@Param("customerId") UUID customerId,
                           @Param("excludeId") UUID excludeId);
}
