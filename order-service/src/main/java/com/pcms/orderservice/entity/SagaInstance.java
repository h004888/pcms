package com.pcms.orderservice.entity;

import com.pcms.common.entity.BaseEntity;
import com.pcms.orderservice.saga.SagaStatus;
import com.pcms.orderservice.saga.SagaType;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One saga instance per distributed transaction (e.g. one Order Fulfillment).
 * Tracks the overall state and aggregates the saga steps.
 *
 * <p>B-17: Saga pattern — central orchestration record.
 * Steps are recorded in the same transaction as state transitions.
 */
@Entity
@Table(name = "saga_instances", indexes = {
        @Index(name = "idx_saga_status", columnList = "status"),
        @Index(name = "idx_saga_aggregate", columnList = "aggregate_type, aggregate_id"),
        @Index(name = "idx_saga_type", columnList = "saga_type")
})
@EntityListeners(AuditingEntityListener.class)
public class SagaInstance extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "saga_type", nullable = false, length = 50)
    private SagaType sagaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SagaStatus status = SagaStatus.STARTED;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @OneToMany(mappedBy = "saga", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<SagaStep> steps = new ArrayList<>();

    public SagaType getSagaType() { return sagaType; }
    public void setSagaType(SagaType sagaType) { this.sagaType = sagaType; }
    public SagaStatus getStatus() { return status; }
    public void setStatus(SagaStatus status) { this.status = status; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public UUID getAggregateId() { return aggregateId; }
    public void setAggregateId(UUID aggregateId) { this.aggregateId = aggregateId; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public List<SagaStep> getSteps() { return steps; }
    public void setSteps(List<SagaStep> steps) { this.steps = steps; }
}