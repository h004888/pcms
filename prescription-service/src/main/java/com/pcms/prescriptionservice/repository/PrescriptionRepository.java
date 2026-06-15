package com.pcms.prescriptionservice.repository;

import com.pcms.prescriptionservice.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    Optional<Prescription> findByCode(String code);

    Page<Prescription> findByPatientId(UUID patientId, Pageable pageable);
    Page<Prescription> findByDoctorId(UUID doctorId, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.code LIKE CONCAT('RX-', :yearPrefix, '%') ORDER BY p.code DESC")
    List<Prescription> findByYearPrefix(String yearPrefix, Pageable pageable);
}
