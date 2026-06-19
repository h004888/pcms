package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.Vaccine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, UUID> {

    Page<Vaccine> findByStatusOrderByNameAsc(String status, Pageable pageable);

    Page<Vaccine> findByStatusAndManufacturerContainingIgnoreCaseOrderByNameAsc(
            String status, String manufacturer, Pageable pageable);
}