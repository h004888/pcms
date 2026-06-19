package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, UUID> {

    List<VoucherUsage> findByCustomerIdOrderByUsedAtDesc(UUID customerId);

    int countByVoucherIdAndCustomerId(UUID voucherId, UUID customerId);
}
