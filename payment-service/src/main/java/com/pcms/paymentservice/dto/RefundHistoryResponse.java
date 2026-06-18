package com.pcms.paymentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Lightweight refund history response for a payment. */
public record RefundHistoryResponse(
        UUID paymentId,
        BigDecimal originalAmount,
        BigDecimal refundedAmount,
        BigDecimal remainingAmount,
        List<RefundEntry> entries) {
    public record RefundEntry(
            BigDecimal amount,
            String reason,
            LocalDateTime refundedAt) {
    }
}