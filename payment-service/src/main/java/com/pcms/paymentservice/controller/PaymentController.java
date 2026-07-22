package com.pcms.paymentservice.controller;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.paymentservice.dto.ConfirmPaymentRequest;
import com.pcms.paymentservice.dto.CreatePaymentRequest;
import com.pcms.common.dto.PageResponse;
import com.pcms.paymentservice.dto.InvoiceResponse;
import com.pcms.paymentservice.dto.PaymentResponse;
import com.pcms.paymentservice.dto.RefundHistoryResponse;
import com.pcms.paymentservice.dto.RefundPaymentRequest;
import com.pcms.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * UC07 - Process Payment (Cash, Card, QR)
 * FR7.1, FR7.2, FR7.3, FR7.4, FR7.5
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<PaymentResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.of(paymentService.list(page, size), p -> p));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @GetMapping("/invoice/{invoiceNumber}")
    public ResponseEntity<PaymentResponse> getByInvoice(@PathVariable String invoiceNumber) {
        return paymentService.list(0, Integer.MAX_VALUE).getContent().stream()
                .filter(p -> invoiceNumber.equals(p.invoiceNumber()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Payment with invoice", invoiceNumber));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId));
    }

    @GetMapping("/status/{orderNumber}")
    public ResponseEntity<java.util.Map<String, Object>> getStatusByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(paymentService.getStatusByOrderNumber(orderNumber));
    }

    /**
     * POST /api/v1/payments - Process a payment
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(request));
    }

    /**
     * POST /api/v1/payments/{id}/confirm - Confirm a pending payment
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable UUID id,
            @RequestBody ConfirmPaymentRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(
                id,
                request.staffId(),
                request.paymentMethod(),
                request.amount(),
                request.tenderedAmount(),
                request.transactionRef()));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refund(@PathVariable UUID id,
            @Valid @RequestBody(required = false) RefundPaymentRequest request) {
        RefundPaymentRequest normalizedRequest = request == null ? new RefundPaymentRequest(null, null) : request;
        return ResponseEntity.ok(paymentService.refund(id, normalizedRequest));
    }

    @PutMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundLegacy(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.softCancel(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.cancelPending(id));
    }

    @GetMapping("/{id}/refund-history")
    public ResponseEntity<RefundHistoryResponse> refundHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.refundHistory(id));
    }

    /**
     * TICKET-204: GET /api/v1/payments/{id}/invoice (SDD §6.9 / SCR-INVOICE).
     * Aggregate Payment + Order + Customer + Branch into a printable invoice.
     */
    @GetMapping("/{id}/invoice")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getInvoice(id));
    }

    /**
     * TICKET-205: POST /api/v1/payments/{id}/print (SDD §6.9 / SCR-INVOICE).
     * Stub: queues a print job on the default printer. In production this
     * would call the printer gateway (LAN/IPP) or return a PDF blob.
     */
    @PostMapping("/{id}/print")
    public ResponseEntity<java.util.Map<String, String>> printInvoice(
            @PathVariable UUID id,
            @RequestParam(value = "printerId", required = false) String printerId) {
        // Validate payment exists
        paymentService.getById(id);
        String resolvedPrinter = printerId == null || printerId.isBlank() ? "default" : printerId;
        // TODO: integrate with printer gateway (IPP/CUPS) in follow-up sprint
        return ResponseEntity.accepted().body(java.util.Map.of(
                "status", "queued",
                "printerId", resolvedPrinter,
                "paymentId", id.toString()));
    }
}
