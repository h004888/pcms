package com.pcms.paymentservice.controller;

import com.pcms.paymentservice.entity.Payment;
import com.pcms.paymentservice.enums.PaymentMethod;
import com.pcms.paymentservice.enums.PaymentStatus;
import com.pcms.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * UC07 - Process Payment (Cash, Card, QR)
 * FR7.1, FR7.2, FR7.3, FR7.4, FR7.5
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return ResponseEntity.ok(Map.of(
            "data", payments.getContent(),
            "page", payments.getNumber(),
            "size", payments.getSize(),
            "total", payments.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getById(@PathVariable UUID id) {
        return paymentRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/invoice/{invoiceNumber}")
    public ResponseEntity<Payment> getByInvoice(@PathVariable String invoiceNumber) {
        return paymentRepository.findByInvoiceNumber(invoiceNumber).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getByOrder(@PathVariable UUID orderId) {
        return paymentRepository.findByOrderId(orderId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/payments - Process a payment
     */
    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody PaymentRequest request) {
        if (request.amount() == null || request.amount().signum() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG33", "message", "Invalid amount"));
        }
        // MSG21: Check tendered for CASH
        if (request.paymentMethod() == PaymentMethod.CASH) {
            if (request.tenderedAmount() == null || request.tenderedAmount().compareTo(request.amount()) < 0) {
                return ResponseEntity.badRequest().body(Map.of("code", "MSG21", "message", "Insufficient tendered amount"));
            }
        }

        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setAmount(request.amount());
        payment.setTenderedAmount(request.tenderedAmount());
        payment.setStaffId(request.staffId());

        // Calculate change for CASH
        if (request.paymentMethod() == PaymentMethod.CASH && request.tenderedAmount() != null) {
            payment.setChangeAmount(request.tenderedAmount().subtract(request.amount()));
        }

        // Mark as SUCCESS (in real impl, would call gateway for CARD/QR)
        payment.setStatus(PaymentStatus.SUCCESS);
        if (request.transactionRef() != null) {
            payment.setTransactionRef(request.transactionRef());
        }

        // TODO: Generate invoice number, then call order-service markOrderPaid
        Payment saved = paymentRepository.save(payment);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/refund")
    public ResponseEntity<?> refund(@PathVariable UUID id) {
        Optional<Payment> optional = paymentRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Payment p = optional.get();
        p.setStatus(PaymentStatus.REFUNDED);
        return ResponseEntity.ok(paymentRepository.save(p));
    }

    public record PaymentRequest(
        UUID orderId,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        BigDecimal tenderedAmount,
        UUID staffId,
        String transactionRef
    ) {}
}
