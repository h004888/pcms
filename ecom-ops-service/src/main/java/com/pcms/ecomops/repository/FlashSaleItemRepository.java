package com.pcms.ecomops.repository;

import com.pcms.ecomops.entity.FlashSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, UUID> {
    List<FlashSaleItem> findByFlashSaleId(UUID flashSaleId);
}
