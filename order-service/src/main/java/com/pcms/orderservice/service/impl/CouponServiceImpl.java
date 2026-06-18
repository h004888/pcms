package com.pcms.orderservice.service.impl;

import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.orderservice.dto.CouponResponse;
import com.pcms.orderservice.dto.CreateCouponRequest;
import com.pcms.orderservice.dto.UpdateCouponRequest;
import com.pcms.orderservice.entity.Coupon;
import com.pcms.orderservice.enums.CouponStatus;
import com.pcms.orderservice.enums.CouponType;
import com.pcms.orderservice.repository.CouponRepository;
import com.pcms.orderservice.service.CouponService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    public CouponServiceImpl(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Override
    public CouponResponse create(CreateCouponRequest request) {
        validateDateRange(request.validFrom(), request.validTo());
        couponRepository.findByCode(request.code()).ifPresent(existing -> {
            throw new DuplicateResourceException("Coupon code", request.code());
        });

        Coupon coupon = new Coupon();
        coupon.setCode(request.code().trim().toUpperCase());
        coupon.setDescription(request.description().trim());
        coupon.setCouponType(request.couponType());
        coupon.setValue(request.value());
        coupon.setValidFrom(request.validFrom());
        coupon.setValidTo(request.validTo());
        coupon.setMaxUses(request.maxUses());
        coupon.setStatus(CouponStatus.ACTIVE);
        return CouponResponse.from(couponRepository.save(coupon));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> list() {
        return couponRepository.findAll().stream().map(CouponResponse::from).toList();
    }

    @Override
    public CouponResponse update(UUID id, UpdateCouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        if (request.description() != null) {
            coupon.setDescription(request.description().trim());
        }
        if (request.couponType() != null) {
            coupon.setCouponType(request.couponType());
        }
        if (request.value() != null) {
            coupon.setValue(request.value());
        }
        if (request.validFrom() != null) {
            coupon.setValidFrom(request.validFrom());
        }
        if (request.validTo() != null) {
            coupon.setValidTo(request.validTo());
        }
        validateDateRange(coupon.getValidFrom(), coupon.getValidTo());
        if (request.maxUses() != null) {
            coupon.setMaxUses(request.maxUses());
        }
        if (request.status() != null) {
            coupon.setStatus(request.status());
        }
        return CouponResponse.from(couponRepository.save(coupon));
    }

    @Override
    public void deactivate(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", id));
        coupon.setStatus(CouponStatus.INACTIVE);
        couponRepository.save(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon findApplicableCoupon(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        Coupon coupon = couponRepository
                .findApplicableCoupon(code.trim().toUpperCase(), CouponStatus.ACTIVE, LocalDateTime.now())
                .orElseThrow(() -> new InvalidOperationException(
                        "Coupon is invalid or expired",
                        "Coupon không hợp lệ hoặc đã hết hạn"));
        if (coupon.getMaxUses() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            throw new InvalidOperationException(
                    "Coupon has reached maximum usage",
                    "Coupon đã đạt giới hạn sử dụng");
        }
        return coupon;
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal amount) {
        if (coupon == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = coupon.getCouponType() == CouponType.PERCENTAGE
                ? amount.multiply(coupon.getValue()).divide(BigDecimal.valueOf(100))
                : coupon.getValue();
        return discount.min(amount).max(BigDecimal.ZERO);
    }

    @Override
    public void incrementUsage(Coupon coupon) {
        if (coupon == null) {
            return;
        }
        coupon.setUsedCount((coupon.getUsedCount() == null ? 0 : coupon.getUsedCount()) + 1);
        couponRepository.save(coupon);
    }

    private void validateDateRange(LocalDateTime validFrom, LocalDateTime validTo) {
        if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
            throw new InvalidOperationException(
                    "Coupon validFrom must be before validTo",
                    "Ngày bắt đầu của coupon phải trước ngày kết thúc");
        }
    }
}