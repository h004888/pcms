package com.pcms.orderservice.entity;

import com.pcms.common.entity.BaseEntity;
import com.pcms.orderservice.saga.SagaStepStatus;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Records one step (forward or compensation) in a SagaInstance.
 * Forward step = the saga orchestrator's planned action.
 * Compensating step = the undo for a forward step (referenced by
 * {@link #compensatesStepId}).
 *
 * <p>B-17: Saga pattern — step record with explicit ordering and status.
 */
@Entity
@Table(name = "saga_steps", indexes = {
        @Index(name = "idx_step_saga", columnList = "saga_id, step_order"),
        @Index(name = "idx_step_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class SagaStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "saga_id", nullable = false)
    private SagaInstance saga;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_name", nullable = false, length = 80)
    private String stepName;

    @Column(name = "target_service", nullable = false, length = 50)
    private String targetService;

    @Column(name = "endpoint", nullable = false, length = 200)
    private String endpoint;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "is_compensation", nullable = false)
    private Boolean compensation = false;

    /** Set when this is a compensation step. Points to the forward step being undone. */
    @Column(name = "compensates_step_id")
    private UUID compensatesStepId;

    @Column(name = "outbox_event_id")
    private UUID outboxEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SagaStepStatus status = SagaStepStatus.PENDING;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    public SagaInstance getSaga() { return saga; }
    public void setSaga(SagaInstance saga) { this.saga = saga; }
    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getTargetService() { return targetService; }
    public void setTargetService(String targetService) { this.targetService = targetService; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Boolean getCompensation() { return compensation; }
    public void setCompensation(Boolean compensation) { this.compensation = compensation; }
    public UUID getCompensatesStepId() { return compensatesStepId; }
    public void setCompensatesStepId(UUID compensatesStepId) { this.compensatesStepId = compensatesStepId; }
    public UUID getOutboxEventId() { return outboxEventId; }
    public void setOutboxEventId(UUID outboxEventId) { this.outboxEventId = outboxEventId; }
    public SagaStepStatus getStatus() { return status; }
    public void setStatus(SagaStepStatus status) { this.status = status; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
}