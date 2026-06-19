package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.BatchVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BatchVerificationRepository extends JpaRepository<BatchVerification, UUID> {
    Optional<BatchVerification> findByBatchNo(String batchNo);
}