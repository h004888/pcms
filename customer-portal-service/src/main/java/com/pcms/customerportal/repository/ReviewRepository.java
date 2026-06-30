package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Review> findByMedicineIdOrderByCreatedAtDesc(UUID medicineId);

    Optional<Review> findByCustomerIdAndMedicineId(UUID customerId, UUID medicineId);
}