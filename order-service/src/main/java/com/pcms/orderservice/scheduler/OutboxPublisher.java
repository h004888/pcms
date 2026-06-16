package com.pcms.orderservice.scheduler;

import com.pcms.orderservice.entity.OutboxEvent;
import com.pcms.orderservice.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox event publisher (B-17).
 *
 * <p>Every 30 seconds, picks up PENDING events from the outbox table and
 * dispatches them via REST call to the target service. On success, marks
 * SENT. On failure, increments retry_count (will be retried next cycle).
 *
 * <p>Replace {@code RestTemplate} with a Feign client when inter-service
 * HTTP is more standardized.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 5;

    private final OutboxEventRepository outboxRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.services.inventory-url:http://localhost:8086}")
    private String inventoryServiceUrl;

    @Value("${app.services.customer-url:http://localhost:8087}")
    private String customerServiceUrl;

    @Autowired
    public OutboxPublisher(OutboxEventRepository outboxRepo) {
        this.outboxRepo = outboxRepo;
    }

    @Scheduled(fixedRate = 30_000)  // every 30 seconds
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = outboxRepo
                .findByStatusOrderByCreatedAtAsc(OutboxEvent.Status.PENDING, PageRequest.of(0, BATCH_SIZE));
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
            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());
                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus(OutboxEvent.Status.FAILED);
                    log.error("[outbox] Event {} failed after {} retries: {}",
                            event.getId(), MAX_RETRIES, e.getMessage());
                } else {
                    log.warn("[outbox] Event {} failed (retry {}/{}): {}",
                            event.getId(), event.getRetryCount(), MAX_RETRIES, e.getMessage());
                }
                outboxRepo.save(event);
            }
        }
    }

    private void dispatch(OutboxEvent event) {
        String baseUrl = resolveServiceUrl(event.getTargetService());
        String url = baseUrl + event.getEndpoint();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Forward idempotency key if present
        if (event.getEventType() != null) {
            headers.set("X-Outbox-Event-Id", event.getId().toString());
        }
        HttpEntity<String> entity = new HttpEntity<>(event.getPayload(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RestClientException("Non-2xx response: " + response.getStatusCode());
        }
    }

    private String resolveServiceUrl(String serviceName) {
        return switch (serviceName) {
            case "inventory-service" -> inventoryServiceUrl;
            case "customer-service" -> customerServiceUrl;
            default -> throw new IllegalArgumentException("Unknown service: " + serviceName);
        };
    }
}
