package com.pcms.customerportal.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.customerportal.dto.request.ApplyVoucherRequest;
import com.pcms.customerportal.dto.response.ApplyVoucherResponse;
import com.pcms.customerportal.dto.response.VoucherResponse;
import com.pcms.customerportal.dto.response.VoucherUsageResponse;
import com.pcms.customerportal.entity.Voucher;
import com.pcms.customerportal.entity.VoucherUsage;
import com.pcms.customerportal.enums.VoucherStatus;
import com.pcms.customerportal.enums.VoucherType;
import com.pcms.customerportal.repository.VoucherRepository;
import com.pcms.customerportal.repository.VoucherUsageRepository;
import com.pcms.customerportal.service.VoucherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VoucherServiceImpl implements VoucherService {

    private static final Logger log = LoggerFactory.getLogger(VoucherServiceImpl.class);

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;

    public VoucherServiceImpl(VoucherRepository voucherRepository,
                               VoucherUsageRepository voucherUsageRepository) {
        this.voucherRepository = voucherRepository;
        this.voucherUsageRepository = voucherUsageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> listActive() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findByStatus(VoucherStatus.ACTIVE).stream()
                .filter(v -> v.getValidFrom().isBefore(now) && v.getValidTo().isAfter(now))
                .map(VoucherResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public ApplyVoucherResponse apply(UUID customerId, ApplyVoucherRequest request, BigDecimal cartTotal) {
        if (customerId == null) {
            throw new InvalidOperationException("customerId is required", "Thiếu thông tin khách hàng");
        }

        Voucher voucher = voucherRepository.findByCode(request.voucherCode())
                .orElseThrow(() -> new InvalidOperationException(
                        "Voucher not found",
                        "Không tìm thấy mã giảm giá"));

        // Validate status
        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            return ApplyVoucherResponse.invalid("Voucher is not active");
        }

        // Validate date range
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getValidFrom().isAfter(now)) {
            return ApplyVoucherResponse.invalid("Voucher has not started yet");
        }
        if (voucher.getValidTo().isBefore(now)) {
            return ApplyVoucherResponse.invalid("Voucher has expired");
        }

        // Validate usage limit
        if (voucher.getUsageLimit() > 0 && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return ApplyVoucherResponse.invalid("Voucher usage limit reached");
        }

        // Validate per-user limit
        int userUsage = voucherUsageRepository.countByVoucherIdAndCustomerId(voucher.getId(), customerId);
        if (userUsage >= voucher.getPerUserLimit()) {
            return ApplyVoucherResponse.invalid("You have already used this voucher");
        }

        // Validate min order amount
        if (cartTotal.compareTo(voucher.getMinOrderAmount()) < 0) {
            return ApplyVoucherResponse.invalid(
                    "Minimum order amount is " + voucher.getMinOrderAmount() + " VND");
        }

        // Calculate discount
        BigDecimal discount;
        switch (voucher.getType()) {
            case PERCENT -> {
                discount = cartTotal.multiply(voucher.getValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                    discount = voucher.getMaxDiscount();
                }
            }
            case FIXED -> {
                discount = voucher.getValue();
                if (discount.compareTo(cartTotal) > 0) {
                    discount = cartTotal;
                }
            }
            case FREE_SHIP -> {
                // Shipping fee handled elsewhere; just signal valid
                discount = BigDecimal.ZERO;
            }
            default -> discount = BigDecimal.ZERO;
        }

        BigDecimal newTotal = cartTotal.subtract(discount).max(BigDecimal.ZERO);

        log.info("[Voucher] applied code={} type={} discount={} for customer={}",
                voucher.getCode(), voucher.getType(), discount, customerId);

        return ApplyVoucherResponse.valid(discount, newTotal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherUsageResponse> history(UUID customerId) {
        if (customerId == null) {
            throw new InvalidOperationException("customerId is required", "Thiếu thông tin khách hàng");
        }
        return voucherUsageRepository.findByCustomerIdOrderByUsedAtDesc(customerId).stream()
                .map(VoucherUsageResponse::from)
                .toList();
    }
}
