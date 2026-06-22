package com.pcms.paymentservice.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.paymentservice.client.OrderClient;
import com.pcms.paymentservice.entity.OutboxEvent;
import com.pcms.paymentservice.repository.OutboxEventRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Outbox publisher for payment-service (B-17, payment → order bridge).
 *
 * <p>
 * Every 30 seconds, picks up PENDING outbox events and dispatches them via
 * Feign to the target service. On success, marks SENT. On failure, increments
 * retry_count with exponential backoff (1s, 5s, 30s, 2m, 10m). After
 * MAX_RETRIES attempts, marks FAILED.
 *
 * <p>
 * Currently supports a single target: order-service. The PAYMENT_COMPLETED
 * event triggers {@code orderService.markOrderPaid(...)} which kicks off the
 * Order Fulfillment Saga in order-service (stock consume + points + notify).
 */
@Component
public class PaymentOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentOutboxPublisher.class);
    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 5;

    private final OutboxEventRepository outboxRepo;
    private final OrderClient orderClient;
    private final ObjectMapper objectMapper;

    public PaymentOutboxPublisher(OutboxEventRepository outboxRepo,
            OrderClient orderClient, ObjectMapper objectMapper) {
        this.outboxRepo = outboxRepo;
        this.orderClient = orderClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 30_000) // every 30 seconds
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxRepo.findReadyToPublish(
                OutboxEvent.Status.PENDING, LocalDateTime.now(), PageRequest.of(0, BATCH_SIZE));
        if (pending.isEmpty()) {
            return;
        }
        log.info("[payment-outbox] Publishing {} pending events", pending.size());
        for (OutboxEvent event : pending) {
            try {
                dispatch(event);
                event.setStatus(OutboxEvent.Status.SENT);
                event.setSentAt(LocalDateTime.now());
                outboxRepo.save(event);
                log.info("[payment-outbox] Event {} ({}) SENT", event.getId(), event.getEventType());
            } catch (FeignException | InvalidOperationException e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());
                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus(OutboxEvent.Status.FAILED);
                    log.error("[payment-outbox] Event {} failed after {} retries: {}",
                            event.getId(), MAX_RETRIES, e.getMessage());
                } else {
                    event.setNextAttemptAt(
                            LocalDateTime.now().plusSeconds(calculateBackoffSeconds(event.getRetryCount())));
                    log.warn("[payment-outbox] Event {} failed (retry {}/{}): {}",
                            event.getId(), event.getRetryCount(), MAX_RETRIES, e.getMessage());
                }
                outboxRepo.save(event);
            }
        }
    }

    private void dispatch(OutboxEvent event) {
        if (!"order-service".equals(event.getTargetService())) {
            throw new InvalidOperationException(
                    "Unknown payment outbox target: " + event.getTargetService(),
                    "Outbox payment chưa hỗ trợ target: " + event.getTargetService());
        }
        UUID orderId = event.getAggregateId();
        Map<String, Object> payload = parsePayload(event);
        UUID actorId = payload.get("actorId") != null
                ? UUID.fromString(payload.get("actorId").toString())
                : null;
        orderClient.markOrderPaid(orderId, actorId);
    }

    private Map<String, Object> parsePayload(OutboxEvent event) {
        try {
            return objectMapper.readValue(event.getPayload(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new InvalidOperationException(
                    "Invalid outbox payload for event " + event.getId(),
                    "Payload outbox payment không hợp lệ");
        }
    }

    private long calculateBackoffSeconds(int retryCount) {
        return switch (retryCount) {
            case 1 -> 1L;
            case 2 -> 5L;
            case 3 -> 30L;
            case 4 -> 120L;
            default -> 600L;
        };
    }
}