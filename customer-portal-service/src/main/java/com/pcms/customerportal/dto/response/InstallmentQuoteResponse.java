package com.pcms.customerportal.dto.response;

import java.math.BigDecimal;

public record InstallmentQuoteResponse(
        BigDecimal monthlyPayment,
        BigDecimal interestRate,
        BigDecimal totalInterest,
        BigDecimal totalPayment,
        String provider,
        int months
) {}
