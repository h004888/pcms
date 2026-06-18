package com.pcms.orderservice.service;

import com.pcms.orderservice.dto.CouponResponse;
import com.pcms.orderservice.dto.CreateCouponRequest;
import com.pcms.orderservice.dto.UpdateCouponRequest;
import com.pcms.orderservice.entity.Coupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CouponService {

    CouponResponse create(CreateCouponRequest request);

    List<CouponResponse> list();

    CouponResponse update(UUID id, UpdateCouponRequest request);

    void deactivate(UUID id);

    Coupon findApplicableCoupon(String code);

    BigDecimal calculateDiscount(Coupon coupon, BigDecimal amount);

    void incrementUsage(Coupon coupon);
}