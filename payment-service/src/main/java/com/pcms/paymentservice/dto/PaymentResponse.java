package com.pcms.paymentservice.dto;

import com.pcms.paymentservice.entity.Payment;
import com.pcms.paymentservice.enums.PaymentMethod;
import com.pcms.paymentservice.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a Payment.
 */
public record PaymentResponse(
    UUID id,
    UUID orderId,
    String invoiceNumber,
    PaymentMethod paymentMethod,
    BigDecimal amount,
    BigDecimal tenderedAmount,
    BigDecimal changeAmount,
    String transactionRef,
    PaymentStatus status,
    LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
            p.getId(),
            p.getOrderId(),
            p.getInvoiceNumber(),
            p.getPaymentMethod(),
            p.getAmount(),
            p.getTenderedAmount(),
            p.getChangeAmount(),
            p.getTransactionRef(),
            p.getStatus(),
            p.getCreatedAt()
        );
    }
}
