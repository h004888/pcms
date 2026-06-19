package com.pcms.pharmacistworkbench.repository;

import com.pcms.pharmacistworkbench.entity.VipMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VipMarkRepository extends JpaRepository<VipMark, UUID> {
    Optional<VipMark> findByCustomerId(UUID customerId);
    List<VipMark> findByTierOrderByLoyaltyScoreDesc(String tier);
    List<VipMark> findAllByOrderByLoyaltyScoreDesc();
}
