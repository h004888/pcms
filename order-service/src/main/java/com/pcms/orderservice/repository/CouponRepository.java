package com.pcms.orderservice.repository;

import com.pcms.orderservice.entity.Coupon;
import com.pcms.orderservice.enums.CouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Optional<Coupon> findByCode(String code);

    @Query("""
            SELECT c FROM Coupon c
            WHERE c.code = :code
              AND c.status = :status
              AND c.validFrom <= :now
              AND c.validTo >= :now
            """)
    Optional<Coupon> findApplicableCoupon(String code, CouponStatus status, LocalDateTime now);

    List<Coupon> findByStatusOrderByCreatedAtDesc(CouponStatus status);
}