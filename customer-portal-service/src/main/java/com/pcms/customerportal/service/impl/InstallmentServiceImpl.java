package com.pcms.customerportal.service.impl;

import com.pcms.customerportal.dto.request.InstallmentConfirmRequest;
import com.pcms.customerportal.dto.request.InstallmentQuoteRequest;
import com.pcms.customerportal.dto.response.InstallmentConfirmResponse;
import com.pcms.customerportal.dto.response.InstallmentQuoteResponse;
import com.pcms.customerportal.service.InstallmentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Mock installment provider integration.
 * - Home Credit: 1.5%/month
 * - FE Credit: 1.2%/month
 * PMT formula: P * r * (1+r)^n / ((1+r)^n - 1)
 * where r = monthly_rate, n = months, P = principal
 */
@Service
public class InstallmentServiceImpl implements InstallmentService {

    private static final BigDecimal HOME_CREDIT_RATE = new BigDecimal("0.015");  // 1.5%/month
    private static final BigDecimal FE_CREDIT_RATE   = new BigDecimal("0.012");  // 1.2%/month

    @Override
    public InstallmentQuoteResponse quote(InstallmentQuoteRequest request) {
        BigDecimal monthlyRate = "HOME_CREDIT".equalsIgnoreCase(request.provider())
                ? HOME_CREDIT_RATE
                : FE_CREDIT_RATE;
        int n = request.months();
        BigDecimal r = monthlyRate;
        BigDecimal p = request.amount();

        // PMT = P * r * (1+r)^n / ((1+r)^n - 1)
        BigDecimal onePlusRPowN = BigDecimal.ONE.add(r).pow(n);
        BigDecimal monthlyPayment = p.multiply(r).multiply(onePlusRPowN)
                .divide(onePlusRPowN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
        BigDecimal totalPayment = monthlyPayment.multiply(BigDecimal.valueOf(n));
        BigDecimal totalInterest = totalPayment.subtract(p);

        return new InstallmentQuoteResponse(
                monthlyPayment,
                monthlyRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
                totalInterest.setScale(2, RoundingMode.HALF_UP),
                totalPayment.setScale(2, RoundingMode.HALF_UP),
                request.provider(),
                n
        );
    }

    @Override
    public InstallmentConfirmResponse confirm(InstallmentConfirmRequest request) {
        // Stub: in production, call Home Credit / FE Credit API
        return new InstallmentConfirmResponse("PENDING", UUID.randomUUID().toString());
    }
}