package com.pcms.paymentservice.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Outbox event for payment-service (B-17, payment → order bridge).
 *
 * <p>
 * When a payment succeeds, we persist an {@code OutboxEvent} row in the same
 * transaction as the {@code Payment}. A background publisher picks up PENDING
 * events and dispatches them via Feign clients to order-service. This avoids
 * synchronous cross-service calls that could leave inconsistent state if the
 * network fails mid-call.
 *
 * <p>
 * Guarantees:
 * <ul>
 * <li>Atomicity: event is committed atomically with the payment state change.</li>
 * <li>At-least-once: publisher retries until status = SENT (or FAILED after MAX_RETRIES).</li>
 * <li>Idempotency: order-service side is naturally idempotent because the saga
 *     orchestrator checks for an existing saga per order before starting.</li>
 * </ul>
 */
@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "status, created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class OutboxEvent {

    public enum Status {
        PENDING, SENT, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType; // e.g. "Payment"

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // e.g. "PAYMENT_COMPLETED"

    @Column(name = "target_service", nullable = false, length = 50)
    private String targetService; // e.g. "order-service"

    @Column(name = "endpoint", nullable = false, length = 200)
    private String endpoint; // e.g. "/orders/{uuid}/pay"

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload; // JSON

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public OutboxEvent() {
    }

    public OutboxEvent(String aggregateType, UUID aggregateId, String eventType,
            String targetService, String endpoint, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.targetService = targetService;
        this.endpoint = endpoint;
        this.payload = payload;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(LocalDateTime nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}