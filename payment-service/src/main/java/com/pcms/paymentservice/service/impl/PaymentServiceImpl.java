package com.pcms.paymentservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.paymentservice.client.OrderClient;
import com.pcms.paymentservice.dto.CreatePaymentRequest;
import com.pcms.paymentservice.dto.InvoiceResponse;
import com.pcms.paymentservice.dto.PaymentResponse;
import com.pcms.paymentservice.dto.RefundHistoryResponse;
import com.pcms.paymentservice.dto.RefundPaymentRequest;
import com.pcms.paymentservice.entity.OutboxEvent;
import com.pcms.paymentservice.entity.Payment;
import com.pcms.paymentservice.enums.PaymentMethod;
import com.pcms.paymentservice.enums.PaymentStatus;
import com.pcms.paymentservice.repository.OutboxEventRepository;
import com.pcms.paymentservice.repository.PaymentRepository;
import com.pcms.paymentservice.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link PaymentService} (UC07).
 *
 * <p>
 * FR7.1 CASH, FR7.2 CARD, FR7.3 QR.
 * <br>
 * MSG21: insufficient tendered for CASH.
 * <br>
 * NSF-11: invoice number INV-yyyymmdd-####.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final OutboxEventRepository outboxEventRepository;

    @Value("${payment.gateway.timeout-ms:5000}")
    private long gatewayTimeoutMs;

    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderClient orderClient,
            OutboxEventRepository outboxEventRepository) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return paymentRepository.findAll(pageable).map(PaymentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getById(UUID id) {
        return paymentRepository.findById(id)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Payment for order", orderId));
    }

    @Override
    @Transactional
    public PaymentResponse create(CreatePaymentRequest request) {
        // Validation
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new InvalidOperationException(
                    "Invalid amount",
                    "Số tiền không hợp lệ");
        }
        if (request.paymentMethod() == PaymentMethod.CASH) {
            if (request.tenderedAmount() == null || request.tenderedAmount().compareTo(request.amount()) < 0) {
                throw new InvalidOperationException(
                        "Insufficient tendered amount",
                        "Số tiền khách đưa không đủ");
            }
        } else if (request.paymentMethod() == PaymentMethod.CARD || request.paymentMethod() == PaymentMethod.QR) {
            if (request.transactionRef() == null || request.transactionRef().isBlank()) {
                throw new InvalidOperationException(
                        "Transaction reference is required for CARD/QR payments",
                        "Cần mã giao dịch cho thanh toán thẻ/QR");
            }
        }

        // Verify the order exists by calling order-service
        // Note: OrderClient does not expose a getOrderById endpoint; this is the
        // responsibility of the order-service. The payment amount is provided by
        // the client; the order-service will validate on markOrderPaid().
        // We still validate the amount is positive and tendered is sufficient above.

        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setAmount(request.amount());
        payment.setTenderedAmount(request.tenderedAmount());
        payment.setStaffId(request.staffId());
        payment.setInvoiceNumber(generateInvoiceNumber());
        payment.setStatus(request.staffId() != null ? PaymentStatus.SUCCESS : PaymentStatus.PENDING);
        if (request.transactionRef() != null) {
            payment.setTransactionRef(request.transactionRef().replace("-", ""));
        }
        // Calculate change for CASH
        if (request.paymentMethod() == PaymentMethod.CASH && request.tenderedAmount() != null) {
            payment.setChangeAmount(request.tenderedAmount().subtract(request.amount()));
        }

        Payment saved = paymentRepository.save(payment);

        // B-17: Emit OutboxEvent only for in-store payments (staff present).
        // B2C payments wait for webhook; webhook will emit outbox after payment confirmed.
        if (request.staffId() != null) {
            outboxEventRepository.save(new OutboxEvent(
                    "Order", request.orderId(), "PAYMENT_COMPLETED",
                    "order-service",
                    "/orders/" + request.orderId() + "/pay",
                    String.format("{\"actorId\":\"%s\"}", request.staffId())));
            log.info("[payment-outbox] Emitted PAYMENT_COMPLETED for order {} (payment {})",
                    request.orderId(), saved.getId());
        }

        return PaymentResponse.from(saved);
    }

    @Override
    @Transactional
    public PaymentResponse softCancel(UUID id) {
        return refund(id, new RefundPaymentRequest(null, "Soft cancel"));
    }

    @Override
    @Transactional
    public PaymentResponse refund(UUID id, RefundPaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new InvalidOperationException(
                    "Only successful payments can be refunded",
                    "Chỉ có thể hoàn tiền thanh toán thành công");
        }
        BigDecimal alreadyRefunded = payment.getRefundedAmount() == null ? BigDecimal.ZERO
                : payment.getRefundedAmount();
        BigDecimal remaining = payment.getAmount().subtract(alreadyRefunded);
        BigDecimal refundAmount = request.amount() == null ? remaining : request.amount();
        if (refundAmount.signum() <= 0 || refundAmount.compareTo(remaining) > 0) {
            throw new InvalidOperationException(
                    "Refund amount exceeds remaining payment amount",
                    "Số tiền hoàn vượt quá số tiền còn lại");
        }
        BigDecimal totalRefunded = alreadyRefunded.add(refundAmount);
        payment.setRefundedAmount(totalRefunded);
        payment.setRefundReason(request.reason());
        payment.setRefundedAt(LocalDateTime.now());
        payment.setStatus(totalRefunded.compareTo(payment.getAmount()) >= 0
                ? PaymentStatus.REFUNDED
                : PaymentStatus.PARTIALLY_REFUNDED);
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public RefundHistoryResponse refundHistory(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
        BigDecimal refunded = payment.getRefundedAmount() == null ? BigDecimal.ZERO : payment.getRefundedAmount();
        List<RefundHistoryResponse.RefundEntry> entries = payment.getRefundedAt() == null
                ? List.of()
                : List.of(new RefundHistoryResponse.RefundEntry(
                        refunded,
                        payment.getRefundReason(),
                        payment.getRefundedAt()));
        return new RefundHistoryResponse(
                payment.getId(),
                payment.getAmount(),
                refunded,
                payment.getAmount().subtract(refunded),
                entries);
    }

    /**
     * TICKET-204: SDD §6.9 GET /payments/{id}/invoice (SCR-INVOICE).
     * Aggregate Payment + Order + OrderItems + Customer + Branch.
     *
     * <p>Currently uses minimal data (Payment entity only) because
     * OrderClient doesn't expose order/customer/branch lookups yet.
     * TODO: when order-service exposes GET /orders/{id} (and customer
     * / branch lookup via Feign), populate items/customerName/branchName
     * by calling those endpoints. For now, return minimal invoice
     * suitable for PDF rendering with payment-level data.
     */
    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        // Best-effort fetch from order-service; if it fails, return minimal.
        try {
            // OrderClient doesn't expose getOrderById in current API; use markOrderPaid
            // as a liveness check only. Future: add @GetMapping("/orders/{id}") to
            // OrderClient when order-service exposes it.
            // For now, return minimal invoice.
            log.debug("Building invoice for payment {} (minimal mode)", paymentId);
            return InvoiceResponse.minimal(payment);
        } catch (Exception e) {
            log.warn("Failed to enrich invoice for payment {}: {}", paymentId, e.getMessage());
            return InvoiceResponse.minimal(payment);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getStatusByOrderNumber(String orderNumber) {
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionRef(orderNumber.replace("-", ""));
        if (paymentOpt.isEmpty()) {
            return java.util.Map.of("status", "NOT_FOUND");
        }
        Payment payment = paymentOpt.get();
        String status = payment.getStatus() == PaymentStatus.SUCCESS ? "PAID" : "PENDING";
        return java.util.Map.of("status", status);
    }

    /**
     * NSF-11: Generate invoice number INV-yyyymmdd-####
     */
    private String generateInvoiceNumber() {
        String datePrefix = LocalDate.now().format(DATE_FMT);
        List<Payment> latest = paymentRepository.findByDatePrefix(datePrefix, PageRequest.of(0, 1));
        int nextNum = 1;
        if (!latest.isEmpty()) {
            String inv = latest.get(0).getInvoiceNumber();
            if (inv != null) {
                String numPart = inv.substring(inv.lastIndexOf('-') + 1);
                try {
                    nextNum = Integer.parseInt(numPart) + 1;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("INV-%s-%04d", datePrefix, nextNum);
    }
}