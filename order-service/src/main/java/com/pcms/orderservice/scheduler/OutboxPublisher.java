package com.pcms.orderservice.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.orderservice.client.CustomerPointsClient;
import com.pcms.orderservice.client.InventoryOutboxClient;
import com.pcms.orderservice.client.NotificationOutboxClient;
import com.pcms.orderservice.entity.DeadLetterEvent;
import com.pcms.orderservice.entity.OutboxEvent;
import com.pcms.orderservice.repository.DeadLetterEventRepository;
import com.pcms.orderservice.repository.OutboxEventRepository;
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
 * Outbox event publisher (B-17).
 *
 * <p>
 * Every 30 seconds, picks up PENDING events from the outbox table and
 * dispatches them via REST call to the target service. On success, marks
 * SENT. On failure, increments retry_count (will be retried next cycle).
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 5;

    private final OutboxEventRepository outboxRepo;
    private final DeadLetterEventRepository deadLetterEventRepository;
    private final InventoryOutboxClient inventoryOutboxClient;
    private final CustomerPointsClient customerPointsClient;
    private final NotificationOutboxClient notificationOutboxClient;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxEventRepository outboxRepo,
            DeadLetterEventRepository deadLetterEventRepository,
            InventoryOutboxClient inventoryOutboxClient,
            CustomerPointsClient customerPointsClient,
            NotificationOutboxClient notificationOutboxClient,
            ObjectMapper objectMapper) {
        this.outboxRepo = outboxRepo;
        this.deadLetterEventRepository = deadLetterEventRepository;
        this.inventoryOutboxClient = inventoryOutboxClient;
        this.customerPointsClient = customerPointsClient;
        this.notificationOutboxClient = notificationOutboxClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 30_000) // every 30 seconds
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxRepo
                .findReadyToPublish(OutboxEvent.Status.PENDING, LocalDateTime.now(), PageRequest.of(0, BATCH_SIZE));
        if (pending.isEmpty()) {
            return;
        }
        log.info("[outbox] Publishing {} pending events", pending.size());
        for (OutboxEvent event : pending) {
            try {
                dispatch(event);
                event.setStatus(OutboxEvent.Status.SENT);
                event.setSentAt(LocalDateTime.now());
                outboxRepo.save(event);
            } catch (FeignException | InvalidOperationException e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());
                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus(OutboxEvent.Status.FAILED);
                    deadLetterEventRepository.save(new DeadLetterEvent(event));
                    log.error("[outbox] Event {} failed after {} retries: {}",
                            event.getId(), MAX_RETRIES, e.getMessage());
                } else {
                    event.setNextAttemptAt(
                            LocalDateTime.now().plusSeconds(calculateBackoffSeconds(event.getRetryCount())));
                    log.warn("[outbox] Event {} failed (retry {}/{}): {}",
                            event.getId(), event.getRetryCount(), MAX_RETRIES, e.getMessage());
                }
                outboxRepo.save(event);
            }
        }
    }

    private void dispatch(OutboxEvent event) {
        Map<String, Object> payload = parsePayload(event);
        String eventId = event.getId().toString();
        switch (event.getTargetService()) {
            case "inventory-service" -> dispatchInventory(event, eventId, payload);
            case "customer-service" -> customerPointsClient.addPoints(eventId, extractCustomerId(event), payload);
            case "notification-service" -> notificationOutboxClient.orderPaid(eventId, payload);
            default -> throw new InvalidOperationException(
                    "Unknown outbox target service: " + event.getTargetService(),
                    "Không xác định được service nhận outbox event");
        }
    }

    private void dispatchInventory(OutboxEvent event, String eventId, Map<String, Object> payload) {
        UUID orderId = event.getAggregateId();
        if (event.getEndpoint().endsWith("/cancelled")) {
            inventoryOutboxClient.orderCancelled(eventId, orderId, payload);
            return;
        }
        inventoryOutboxClient.orderPaid(eventId, orderId, payload);
    }

    private UUID extractCustomerId(OutboxEvent event) {
        String[] parts = event.getEndpoint().split("/");
        if (parts.length < 3) {
            throw new InvalidOperationException(
                    "Invalid customer outbox endpoint: " + event.getEndpoint(),
                    "Endpoint outbox của khách hàng không hợp lệ");
        }
        return UUID.fromString(parts[2]);
    }

    private Map<String, Object> parsePayload(OutboxEvent event) {
        try {
            return objectMapper.readValue(event.getPayload(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new InvalidOperationException(
                    "Invalid outbox payload for event " + event.getId(),
                    "Payload outbox event không hợp lệ");
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
