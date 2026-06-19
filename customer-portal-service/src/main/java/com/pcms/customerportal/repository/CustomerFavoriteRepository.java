package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.CustomerFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerFavoriteRepository extends JpaRepository<CustomerFavorite, UUID> {

    Page<CustomerFavorite> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    Optional<CustomerFavorite> findByCustomerIdAndMedicineId(UUID customerId, UUID medicineId);

    boolean existsByCustomerIdAndMedicineId(UUID customerId, UUID medicineId);

    long deleteByCustomerIdAndMedicineId(UUID customerId, UUID medicineId);
}
