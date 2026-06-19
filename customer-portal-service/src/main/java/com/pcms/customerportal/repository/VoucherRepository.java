package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.Voucher;
import com.pcms.customerportal.enums.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {

    Optional<Voucher> findByCode(String code);

    List<Voucher> findByStatus(VoucherStatus status);

    Optional<Voucher> findByCodeAndStatus(String code, VoucherStatus status);
}
