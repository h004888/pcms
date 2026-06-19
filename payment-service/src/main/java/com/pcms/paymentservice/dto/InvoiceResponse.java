package com.pcms.paymentservice.dto;

import com.pcms.paymentservice.entity.Payment;
import com.pcms.paymentservice.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * TICKET-204: Response DTO for GET /payments/{id}/invoice (SCR-INVOICE).
 *
 * <p>Aggregates payment data with order/customer/branch info fetched
 * from order-service. Items and customer/branch details may be null
 * if the order-service is unreachable (resilience via circuit breaker).
 *
 * <p>Aligned with SDD §6.9: invoice number, order number, customer
 * name, branch name, items, totals, payment method, paid at, cashier.
 */
public record InvoiceResponse(
        UUID paymentId,
        String invoiceNumber,
        UUID orderId,
        String orderNumber,
        PaymentMethod paymentMethod,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        BigDecimal tenderedAmount,
        BigDecimal changeAmount,
        String transactionRef,
        UUID customerId,
        String customerName,
        UUID branchId,
        String branchName,
        UUID staffId,
        String staffName,
        List<InvoiceItem> items,
        LocalDateTime paidAt,
        LocalDateTime createdAt) {

    /**
     * Per-line item on the invoice. Populated when order-service is reachable.
     */
    public record InvoiceItem(
            UUID medicineId,
            String medicineName,
            int qty,
            BigDecimal unitPrice,
            BigDecimal discount,
            BigDecimal subtotal) {}

    /**
     * Build a minimal invoice from a Payment entity when remote order-service
     * data is not available (fallback). Items/customer/branch will be null.
     */
    public static InvoiceResponse minimal(Payment p) {
        return new InvoiceResponse(
                p.getId(),
                p.getInvoiceNumber(),
                p.getOrderId(),
                null, // orderNumber — requires order-service
                p.getPaymentMethod(),
                p.getAmount(),
                BigDecimal.ZERO,
                p.getAmount(),
                p.getTenderedAmount(),
                p.getChangeAmount(),
                p.getTransactionRef(),
                null, null, // customer
                null, null, // branch
                p.getStaffId(),
                null, // staffName
                List.of(), // items
                p.getCreatedAt(),
                p.getCreatedAt());
    }
}