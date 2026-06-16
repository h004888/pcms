package com.pcms.paymentservice.dto;

import com.pcms.paymentservice.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for processing a payment (UC07).
 */
public record CreatePaymentRequest(
    @NotNull UUID orderId,
    @NotNull PaymentMethod paymentMethod,
    @NotNull @Positive BigDecimal amount,
    BigDecimal tenderedAmount,
    UUID staffId,
    String transactionRef
) {}
