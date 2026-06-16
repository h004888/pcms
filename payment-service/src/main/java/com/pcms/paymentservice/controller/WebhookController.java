package com.pcms.paymentservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.paymentservice.entity.Payment;
import com.pcms.paymentservice.entity.WebhookEvent;
import com.pcms.paymentservice.enums.PaymentStatus;
import com.pcms.paymentservice.enums.WebhookEventStatus;
import com.pcms.paymentservice.repository.PaymentRepository;
import com.pcms.paymentservice.repository.WebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment gateway webhook receiver (B-09).
 *
 * <p>Gateway calls this endpoint when async payment (CARD/QR) completes.
 * Flow:
 * <ol>
 *   <li>Verify HMAC signature (CR-09 / FR7.2)</li>
 *   <li>Idempotency check by {@code gatewayEventId} — if already processed, return 200 OK without re-processing</li>
 *   <li>Persist {@link WebhookEvent} for audit</li>
 *   <li>Match to internal {@link Payment} by {@code transactionRef} or order id</li>
 *   <li>Update payment status → SUCCESS or FAILED</li>
 *   <li>Trigger order.markAsPaid via OrderClient</li>
 * </ol>
 *
 * <p>Always returns 200 OK for valid (verified + new) events. Returns 4xx
 * for signature failures or malformed bodies. This is intentional so the
 * gateway does not retry successful events.
 */
@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookEventRepository webhookRepo;
    private final PaymentRepository paymentRepo;
    private final com.pcms.paymentservice.client.OrderClient orderClient;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;

    public WebhookController(WebhookEventRepository webhookRepo,
                             PaymentRepository paymentRepo,
                             com.pcms.paymentservice.client.OrderClient orderClient,
                             @Value("${app.payment.webhook-secret:}") String webhookSecret) {
        this.webhookRepo = webhookRepo;
        this.paymentRepo = paymentRepo;
        this.orderClient = orderClient;
        this.objectMapper = new ObjectMapper();
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/payment-gateway")
    @Transactional
    public ResponseEntity<Map<String, Object>> handlePaymentWebhook(
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestHeader(value = "X-Event-Id", required = false) String eventId,
            @RequestBody String rawBody) {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.error("Webhook secret not configured — refusing to process");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "error", "message", "Webhook not configured"));
        }

        // 1) Verify HMAC
        if (signature == null || !verifyHmac(rawBody, signature)) {
            log.warn("Webhook signature verification failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Invalid signature"));
        }

        // 2) Parse body
        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            log.warn("Webhook body parse error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Invalid JSON body"));
        }
        String eventType = textOr(root, "eventType", "payment.success");
        String eventIdValue = eventId != null ? eventId : textOr(root, "id", UUID.randomUUID().toString());

        // 3) Idempotency check
        Optional<WebhookEvent> existing = webhookRepo.findByGatewayEventId(eventIdValue);
        if (existing.isPresent()) {
            log.info("Duplicate webhook event {} — skipping (idempotent)", eventIdValue);
            WebhookEvent evt = existing.get();
            evt.setStatus(WebhookEventStatus.DUPLICATE);
            webhookRepo.save(evt);
            return ResponseEntity.ok(Map.of("status", "duplicate", "eventId", eventIdValue));
        }

        // 4) Persist event
        WebhookEvent evt = new WebhookEvent(eventIdValue, null, eventType, rawBody, signature);
        webhookRepo.save(evt);

        // 5) Map event to internal Payment
        String transactionRef = textOr(root, "transactionRef", null);
        if (transactionRef == null) {
            evt.setStatus(WebhookEventStatus.FAILED);
            evt.setErrorMessage("Missing transactionRef");
            webhookRepo.save(evt);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Missing transactionRef"));
        }

        Optional<Payment> paymentOpt = paymentRepo.findByTransactionRef(transactionRef);
        if (paymentOpt.isEmpty()) {
            evt.setStatus(WebhookEventStatus.FAILED);
            evt.setErrorMessage("No payment found for transactionRef=" + transactionRef);
            webhookRepo.save(evt);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Payment not found"));
        }
        Payment payment = paymentOpt.get();
        evt.setPaymentId(payment.getId());

        // 6) Apply state transition
        try {
            if ("payment.success".equalsIgnoreCase(eventType) || "payment.captured".equalsIgnoreCase(eventType)) {
                payment.setStatus(PaymentStatus.SUCCESS);
                paymentRepo.save(payment);
                // Trigger order-service to mark order as PAID
                try {
                    orderClient.markOrderPaid(payment.getOrderId(), payment.getStaffId());
                } catch (Exception e) {
                    log.warn("Failed to call orderService.markAsPaid for order {}: {}",
                            payment.getOrderId(), e.getMessage());
                }
            } else if ("payment.failed".equalsIgnoreCase(eventType) || "payment.cancelled".equalsIgnoreCase(eventType)) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepo.save(payment);
            } else {
                evt.setStatus(WebhookEventStatus.FAILED);
                evt.setErrorMessage("Unknown event type: " + eventType);
                webhookRepo.save(evt);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("status", "error", "message", "Unknown event type"));
            }
        } catch (Exception e) {
            evt.setStatus(WebhookEventStatus.FAILED);
            evt.setErrorMessage(e.getMessage());
            webhookRepo.save(evt);
            throw new InvalidOperationException("Webhook processing failed: " + e.getMessage(),
                    "Lỗi xử lý webhook: " + e.getMessage());
        }

        evt.setStatus(WebhookEventStatus.PROCESSED);
        evt.setProcessedAt(java.time.LocalDateTime.now());
        webhookRepo.save(evt);
        return ResponseEntity.ok(Map.of("status", "processed", "eventId", eventIdValue));
    }

    private boolean verifyHmac(String body, String providedSignature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            String computed = "sha256=" + HexFormat.of().formatHex(hash);
            // Constant-time compare to prevent timing attacks
            return constantTimeEquals(computed, providedSignature);
        } catch (Exception e) {
            log.error("HMAC verification error: {}", e.getMessage());
            return false;
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }

    private String textOr(JsonNode node, String field, String fallback) {
        return node != null && node.hasNonNull(field) ? node.get(field).asText() : fallback;
    }
}
