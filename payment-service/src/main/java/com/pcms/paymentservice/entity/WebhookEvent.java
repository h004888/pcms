package com.pcms.paymentservice.entity;

import com.pcms.paymentservice.enums.WebhookEventStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Webhook event from payment gateway (B-09).
 * <p>Idempotency: {@code gatewayEventId} is UNIQUE — same event from gateway
 * retried will be detected and skipped.
 */
@Entity
@Table(
    name = "webhook_events",
    uniqueConstraints = @UniqueConstraint(name = "uk_webhook_gateway_event", columnNames = "gateway_event_id")
)
@EntityListeners(AuditingEntityListener.class)
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "gateway_event_id", nullable = false, length = 100)
    private String gatewayEventId;  // unique idempotency key

    @Column(name = "payment_id")
    private UUID paymentId;  // nullable until matched

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;  // payment.success / payment.failed

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "signature", length = 255)
    private String signature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookEventStatus status = WebhookEventStatus.RECEIVED;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(length = 500)
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public WebhookEvent() {}

    public WebhookEvent(String gatewayEventId, UUID paymentId, String eventType,
                        String rawPayload, String signature) {
        this.gatewayEventId = gatewayEventId;
        this.paymentId = paymentId;
        this.eventType = eventType;
        this.rawPayload = rawPayload;
        this.signature = signature;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getGatewayEventId() { return gatewayEventId; }
    public void setGatewayEventId(String gatewayEventId) { this.gatewayEventId = gatewayEventId; }
    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    public WebhookEventStatus getStatus() { return status; }
    public void setStatus(WebhookEventStatus status) { this.status = status; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
