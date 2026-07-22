package com.pcms.paymentservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for confirming a pending payment.
 */
public record ConfirmPaymentRequest(
    UUID staffId,
    String paymentMethod,
    BigDecimal amount,
    BigDecimal tenderedAmount,
    String transactionRef
) {}
