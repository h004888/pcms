package com.pcms.paymentservice.service;

import com.pcms.paymentservice.dto.CreatePaymentRequest;
import com.pcms.paymentservice.dto.PaymentResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Service interface for Payment (UC07).
 */
public interface PaymentService {

    Page<PaymentResponse> list(int page, int size);

    PaymentResponse getById(UUID id);

    PaymentResponse getByOrderId(UUID orderId);

    /** Process a payment (CASH, CARD, QR). Validates, persists, and notifies order-service. */
    PaymentResponse create(CreatePaymentRequest request);

    /** Soft-cancel: mark payment REFUNDED. */
    PaymentResponse softCancel(UUID id);
}
