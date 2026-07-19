package com.pcms.paymentservice.service;

import com.pcms.paymentservice.dto.CreatePaymentRequest;
import com.pcms.paymentservice.dto.InvoiceResponse;
import com.pcms.paymentservice.dto.PaymentResponse;
import com.pcms.paymentservice.dto.RefundHistoryResponse;
import com.pcms.paymentservice.dto.RefundPaymentRequest;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.UUID;

/**
 * Service interface for Payment (UC07).
 */
public interface PaymentService {

    Page<PaymentResponse> list(int page, int size);

    PaymentResponse getById(UUID id);

    PaymentResponse getByOrderId(UUID orderId);

    /**
     * Process a payment (CASH, CARD, QR). Validates, persists, and notifies
     * order-service.
     */
    PaymentResponse create(CreatePaymentRequest request);

    /** Soft-cancel: mark payment REFUNDED. */
    PaymentResponse softCancel(UUID id);

    /** Refund payment partially or fully. */
    PaymentResponse refund(UUID id, RefundPaymentRequest request);

    /** Get lightweight refund history for a payment. */
    RefundHistoryResponse refundHistory(UUID id);

    /**
     * TICKET-204: SDD §6.9 GET /payments/{id}/invoice.
     * Aggregate Payment + Order + OrderItems + Customer + Branch into a
     * printable invoice. Remote data fetched via OrderClient; falls back
     * to minimal response on remote failure.
     */
    InvoiceResponse getInvoice(UUID paymentId);

    /**
     * Query payment status by order number (for VietQR polling).
     * Returns {status: "PENDING"|"PAID"|"NOT_FOUND"}.
     */
    Map<String, Object> getStatusByOrderNumber(String orderNumber);
}
