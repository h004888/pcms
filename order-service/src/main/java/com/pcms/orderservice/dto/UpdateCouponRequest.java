package com.pcms.orderservice.dto;

import com.pcms.orderservice.enums.CouponStatus;
import com.pcms.orderservice.enums.CouponType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateCouponRequest(
        String description,
        CouponType couponType,
        BigDecimal value,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        Integer maxUses,
        CouponStatus status) {
}