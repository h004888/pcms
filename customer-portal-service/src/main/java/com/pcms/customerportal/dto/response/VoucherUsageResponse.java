package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.VoucherUsage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record VoucherUsageResponse(
        UUID id,
        UUID voucherId,
        UUID orderId,
        BigDecimal discountAmount,
        LocalDateTime usedAt
) {
    public static VoucherUsageResponse from(VoucherUsage vu) {
        return new VoucherUsageResponse(
                vu.getId(),
                vu.getVoucherId(),
                vu.getOrderId(),
                vu.getDiscountAmount(),
                vu.getUsedAt()
        );
    }
}
