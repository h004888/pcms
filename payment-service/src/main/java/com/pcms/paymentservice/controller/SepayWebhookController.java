package com.pcms.paymentservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcms.paymentservice.entity.OutboxEvent;
import com.pcms.paymentservice.entity.Payment;
import com.pcms.paymentservice.entity.WebhookEvent;
import com.pcms.paymentservice.enums.PaymentStatus;
import com.pcms.paymentservice.enums.WebhookEventStatus;
import com.pcms.paymentservice.repository.OutboxEventRepository;
import com.pcms.paymentservice.repository.PaymentRepository;
import com.pcms.paymentservice.repository.WebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sepay webhook receiver.
 * <p>
 * Receives bank transfer notifications from Sepay when a customer
 * transfers money to the configured bank account. Matches the transfer
 * content (order number) to a pending Payment record and marks it as
 * SUCCESS, then triggers order-service to mark the order as PAID.
 * <p>
 * Auth: API Key in Authorization header ({@code Apikey <key>}).
 */
@RestController
@RequestMapping("/webhooks/sepay")
public class SepayWebhookController {

    private static final Logger log = LoggerFactory.getLogger(SepayWebhookController.class);

    private final WebhookEventRepository webhookRepo;
    private final PaymentRepository paymentRepo;
    private final OutboxEventRepository outboxRepo;
    private final com.pcms.paymentservice.client.OrderClient orderClient;
    private final ObjectMapper objectMapper;
    private final String configuredApiKey;

    public SepayWebhookController(WebhookEventRepository webhookRepo,
                                  PaymentRepository paymentRepo,
                                  OutboxEventRepository outboxRepo,
                                  com.pcms.paymentservice.client.OrderClient orderClient,
                                  @Value("${app.sepay.webhook-api-key:}") String configuredApiKey) {
        this.webhookRepo = webhookRepo;
        this.paymentRepo = paymentRepo;
        this.outboxRepo = outboxRepo;
        this.orderClient = orderClient;
        this.objectMapper = new ObjectMapper();
        this.configuredApiKey = configuredApiKey;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> handleSepayWebhook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody String rawBody) {

        // 1. Verify API key
        if (!verifyApiKey(authorization)) {
            log.warn("Sepay webhook: invalid or missing API key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "Unauthorized"));
        }

        // 2. Parse JSON body
        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            log.warn("Sepay webhook: JSON parse error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Invalid JSON body"));
        }

        String content = textOr(root, "content", null);
        String transferType = textOr(root, "transferType", null);
        BigDecimal transferAmount = decimalOr(root, "transferAmount", BigDecimal.ZERO);
        String gatewayEventId = textOr(root, "id", String.valueOf(System.currentTimeMillis()));

        // 3. Idempotency check
        Optional<WebhookEvent> existing = webhookRepo.findByGatewayEventId(gatewayEventId);
        if (existing.isPresent()) {
            log.info("Sepay webhook: duplicate event {} — skipping", gatewayEventId);
            return ResponseEntity.ok(Map.of("status", "duplicate"));
        }

        // 4. Persist webhook event for audit
        WebhookEvent evt = new WebhookEvent(gatewayEventId, null, "sepay.transfer", rawBody, null);
        webhookRepo.save(evt);

        // 5. Verify transfer type is incoming
        if (!"in".equals(transferType)) {
            log.info("Sepay webhook: ignoring non-incoming transfer (type={})", transferType);
            evt.setStatus(WebhookEventStatus.FAILED);
            evt.setErrorMessage("Non-incoming transfer: " + transferType);
            webhookRepo.save(evt);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Not an incoming transfer"));
        }

        // 6. Match content to pending Payment
        if (content == null || content.isBlank()) {
            evt.setStatus(WebhookEventStatus.FAILED);
            evt.setErrorMessage("Missing content field");
            webhookRepo.save(evt);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Missing content"));
        }

        // 6. Extract order number from content (first token before space)
        String orderNumber = extractOrderNumber(content);
        log.info("Sepay webhook: extracted orderNumber={} from content", orderNumber);

        // Match content to pending Payment
        Optional<Payment> paymentOpt = paymentRepo.findByTransactionRef(orderNumber);
        if (paymentOpt.isEmpty()) {
            // Bank content may drop hyphens (e.g. ORD202607190012 vs ORD-20260719-0012)
            // Try reconstructing hyphenated format
            String hyphenated = addHyphens(orderNumber);
            if (hyphenated != null) {
                paymentOpt = paymentRepo.findByTransactionRef(hyphenated);
            }
        }
        if (paymentOpt.isEmpty()) {
            // Try matching by UUID (orderId) if orderNumber is a UUID
            try {
                UUID uuid = UUID.fromString(orderNumber);
                paymentOpt = paymentRepo.findByOrderId(uuid);
            } catch (IllegalArgumentException ignored) {
                // Not a UUID, skip
            }
        }
        if (paymentOpt.isEmpty()) {
            log.warn("Sepay webhook: no payment found for orderNumber={} (content={})", orderNumber, content);
            evt.setStatus(WebhookEventStatus.FAILED);
            evt.setErrorMessage("No payment found for content=" + content);
            webhookRepo.save(evt);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "error", "message", "Payment not found"));
        }

        Payment payment = paymentOpt.get();
        evt.setPaymentId(payment.getId());

        // 7. Verify amount
        if (transferAmount.compareTo(payment.getAmount()) < 0) {
            log.warn("Sepay webhook: insufficient amount {} < {}", transferAmount, payment.getAmount());
            evt.setStatus(WebhookEventStatus.FAILED);
            evt.setErrorMessage("Insufficient amount: " + transferAmount + " < " + payment.getAmount());
            webhookRepo.save(evt);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Insufficient transfer amount"));
        }

        // 8. Mark payment as SUCCESS
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionRef(orderNumber);
        paymentRepo.save(payment);

        // 9. Emit outbox to trigger order-service markOrderPaid
        outboxRepo.save(new OutboxEvent(
                "Order", payment.getOrderId(), "PAYMENT_COMPLETED",
                "order-service",
                "/orders/" + payment.getOrderId() + "/pay",
                "{\"actorId\":\"00000000-0000-0000-0000-000000000000\"}"));
        log.info("Sepay webhook: emitted PAYMENT_COMPLETED outbox for order {}",
                payment.getOrderId());

        // 10. Finalize webhook event
        evt.setStatus(WebhookEventStatus.PROCESSED);
        evt.setProcessedAt(LocalDateTime.now());
        webhookRepo.save(evt);

        log.info("Sepay webhook: payment {} marked SUCCESS for order {}",
                payment.getId(), payment.getOrderId());

        return ResponseEntity.ok(Map.of("status", "processed"));
    }

    private boolean verifyApiKey(String authorization) {
        if (configuredApiKey == null || configuredApiKey.isBlank()) {
            log.error("Sepay webhook API key not configured");
            return false;
        }
        if (authorization == null) {
            return false;
        }
        String expected = "Apikey " + configuredApiKey;
        return constantTimeEquals(expected, authorization);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length())
            return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++)
            diff |= a.charAt(i) ^ b.charAt(i);
        return diff == 0;
    }

    private String textOr(JsonNode node, String field, String fallback) {
        return node != null && node.hasNonNull(field) ? node.get(field).asText() : fallback;
    }

    private BigDecimal decimalOr(JsonNode node, String field, BigDecimal fallback) {
        if (node == null || !node.hasNonNull(field)) return fallback;
        try {
            return node.get(field).decimalValue();
        } catch (Exception e) {
            return fallback;
        }
    }

    private String extractOrderNumber(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        int spaceIdx = content.indexOf(' ');
        if (spaceIdx > 0) {
            return content.substring(0, spaceIdx).trim();
        }
        return content.trim();
    }

    private static final Pattern ORDER_NUMBER_NOHYPHEN = Pattern.compile("^(ORD)(\\d{8})(\\d{4})$");

    private String addHyphens(String orderNumber) {
        if (orderNumber == null) return null;
        Matcher m = ORDER_NUMBER_NOHYPHEN.matcher(orderNumber);
        if (m.matches()) {
            return m.group(1) + "-" + m.group(2) + "-" + m.group(3);
        }
        return null;
    }
}
