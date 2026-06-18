package com.pcms.orderservice.dto;

import com.pcms.orderservice.entity.Coupon;
import com.pcms.orderservice.enums.CouponStatus;
import com.pcms.orderservice.enums.CouponType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        String description,
        CouponType couponType,
        BigDecimal value,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        Integer maxUses,
        Integer usedCount,
        CouponStatus status,
        LocalDateTime createdAt) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getCouponType(),
                coupon.getValue(),
                coupon.getValidFrom(),
                coupon.getValidTo(),
                coupon.getMaxUses(),
                coupon.getUsedCount(),
                coupon.getStatus(),
                coupon.getCreatedAt());
    }
}