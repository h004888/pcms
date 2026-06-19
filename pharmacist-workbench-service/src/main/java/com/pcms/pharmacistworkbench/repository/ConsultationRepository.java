package com.pcms.pharmacistworkbench.repository;

import com.pcms.pharmacistworkbench.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
    List<Consultation> findByCustomerIdOrderByStartedAtDesc(UUID customerId);
    List<Consultation> findByPharmacistIdOrderByStartedAtDesc(UUID pharmacistId);
    List<Consultation> findByPharmacistIdAndStatusOrderByStartedAtDesc(UUID pharmacistId, String status);
}
