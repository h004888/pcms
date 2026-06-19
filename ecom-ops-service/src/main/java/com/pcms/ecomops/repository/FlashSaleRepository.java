package com.pcms.ecomops.repository;

import com.pcms.ecomops.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, UUID> {
    List<FlashSale> findByStatusOrderByStartsAtAsc(String status);
    List<FlashSale> findAllByOrderByStartsAtDesc();
}
