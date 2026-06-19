package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.Voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record VoucherResponse(
        UUID id,
        String code,
        String type,
        BigDecimal value,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscount,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        String status
) {
    public static VoucherResponse from(Voucher v) {
        return new VoucherResponse(
                v.getId(),
                v.getCode(),
                v.getType().name(),
                v.getValue(),
                v.getMinOrderAmount(),
                v.getMaxDiscount(),
                v.getValidFrom(),
                v.getValidTo(),
                v.getStatus().name()
        );
    }
}
