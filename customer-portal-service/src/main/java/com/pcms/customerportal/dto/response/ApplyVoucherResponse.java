package com.pcms.customerportal.dto.response;

import java.math.BigDecimal;

public record ApplyVoucherResponse(
        boolean valid,
        BigDecimal discount,
        BigDecimal newTotal,
        String reason
) {
    public static ApplyVoucherResponse valid(BigDecimal discount, BigDecimal newTotal) {
        return new ApplyVoucherResponse(true, discount, newTotal, null);
    }

    public static ApplyVoucherResponse invalid(String reason) {
        return new ApplyVoucherResponse(false, BigDecimal.ZERO, BigDecimal.ZERO, reason);
    }
}
