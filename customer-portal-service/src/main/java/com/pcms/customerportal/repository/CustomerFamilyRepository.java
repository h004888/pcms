package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.CustomerFamily;
import com.pcms.customerportal.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerFamilyRepository extends JpaRepository<CustomerFamily, UUID> {

    List<CustomerFamily> findByOwnerIdAndStatusOrderByCreatedAtDesc(
            UUID ownerId, RecordStatus status);

    Optional<CustomerFamily> findByIdAndStatus(UUID id, RecordStatus status);
}
