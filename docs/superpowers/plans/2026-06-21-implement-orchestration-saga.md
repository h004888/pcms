# Orchestration Saga Pattern Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform PCMS from Outbox-only event choreography to a true Orchestration Saga pattern with state machine, automatic compensation, and stuck-saga detection.

**Architecture:** Add `SagaInstance` and `SagaStep` entities to track saga state. Introduce `OrderSagaOrchestrator` as central coordinator that executes ordered steps with auto-compensation on failure. Each step uses the existing Outbox pattern for delivery and idempotency. Add new bulk endpoints to inventory-service for atomic stock operations.

**Tech Stack:** Java 21, Spring Boot 4.0.7, Spring Data JPA, Hibernate, MySQL, OpenFeign, Resilience4j, Scheduled Jobs

---

## Background: Why We Need This

PCMS currently has:
- ✅ Outbox pattern (atomic local TX + scheduled HTTP relay)
- ✅ Idempotent consumers via `X-Outbox-Event-Id` + `OutboxLog`
- ❌ No saga state machine (only `Order.status` enum)
- ❌ No automatic compensation (only manual `cancel()` for inventory)
- ❌ No stuck-saga detection
- ❌ No compensation for points/notification when downstream fails
- ❌ `restoreStock` is imprecise (adds to single batch, not reverse specific batches)

This plan implements Option 1 from the analysis: full Orchestration Saga.

---

## File Structure

### Files to Create (in order-service)
- `order-service/src/main/java/com/pcms/orderservice/saga/SagaStatus.java` - Saga state enum
- `order-service/src/main/java/com/pcms/orderservice/saga/SagaStepStatus.java` - Step state enum
- `order-service/src/main/java/com/pcms/orderservice/saga/SagaType.java` - Saga types enum
- `order-service/src/main/java/com/pcms/orderservice/entity/SagaInstance.java` - Saga aggregate
- `order-service/src/main/java/com/pcms/orderservice/entity/SagaStep.java` - Step records
- `order-service/src/main/java/com/pcms/orderservice/repository/SagaInstanceRepository.java`
- `order-service/src/main/java/com/pcms/orderservice/repository/SagaStepRepository.java`
- `order-service/src/main/java/com/pcms/orderservice/saga/SagaOrchestrator.java` - Main orchestrator
- `order-service/src/main/java/com/pcms/orderservice/saga/SagaStepExecutor.java` - Executes steps
- `order-service/src/main/java/com/pcms/orderservice/saga/SagaCompensationHandler.java` - Compensation logic
- `order-service/src/main/java/com/pcms/orderservice/saga/dto/SagaInstanceResponse.java`
- `order-service/src/main/java/com/pcms/orderservice/saga/dto/SagaStepResponse.java`
- `order-service/src/main/java/com/pcms/orderservice/saga/controller/SagaAdminController.java` - Admin endpoints
- `order-service/src/main/java/com/pcms/orderservice/scheduler/SagaTimeoutScheduler.java` - Stuck saga detector

### Files to Create (in inventory-service)
- `inventory-service/src/main/java/com/pcms/inventoryservice/dto/BulkConsumeRequest.java`
- `inventory-service/src/main/java/com/pcms/inventoryservice/dto/BulkRestoreRequest.java`

### Files to Modify
- `order-service/.../service/impl/OrderServiceImpl.java` - Hook into saga
- `order-service/.../controller/OutboxAdminController.java` - Add retry-saga endpoint
- `inventory-service/.../controller/OutboxConsumerController.java` - Add bulk endpoints
- `inventory-service/.../service/impl/InventoryServiceImpl.java` - Add bulkConsume + precise restoreStock
- `inventory-service/.../service/impl/OutboxConsumerServiceImpl.java` - Handle bulk + precise restore
- `payment-service/.../service/impl/PaymentServiceImpl.java` - Trigger saga via event

---

## Task 1: Add Saga enums

**Files:**
- Create: `order-service/src/main/java/com/pcms/orderservice/saga/SagaStatus.java`
- Create: `order-service/src/main/java/com/pcms/orderservice/saga/SagaStepStatus.java`
- Create: `order-service/src/main/java/com/pcms/orderservice/saga/SagaType.java`

- [ ] **Step 1: Create the saga package directory**

Run: `mkdir -p "C:/Users/ADMIN/Downloads/temp_v12/pcms/order-service/src/main/java/com/pcms/orderservice/saga"`
Expected: Directory created

- [ ] **Step 2: Create SagaStatus enum**

Write to `order-service/src/main/java/com/pcms/orderservice/saga/SagaStatus.java`:

```java
package com.pcms.orderservice.saga;

/**
 * Lifecycle states for an Order Fulfillment Saga instance.
 * Transitions:
 *   STARTED → IN_PROGRESS → COMPLETED
 *                       ↘ COMPENSATING → COMPENSATED
 *                       ↘ FAILED (terminal, manual intervention)
 */
public enum SagaStatus {
    STARTED,         // Saga created but no steps executed yet
    IN_PROGRESS,     // At least one forward step completed
    COMPLETED,       // All forward steps succeeded (terminal)
    COMPENSATING,    // At least one forward step failed; running compensations
    COMPENSATED,     // All compensations completed (terminal)
    FAILED           // Compensation failed, manual intervention needed (terminal)
}
```

- [ ] **Step 3: Create SagaStepStatus enum**

Write to `order-service/src/main/java/com/pcms/orderservice/saga/SagaStepStatus.java`:

```java
package com.pcms.orderservice.saga;

/**
 * Status of a single SagaStep. Each step records both its forward status
 * and the id of the compensating step (if any) for reversal.
 */
public enum SagaStepStatus {
    PENDING,         // Step not yet executed
    IN_PROGRESS,     // Step execution started (e.g., outbox event published)
    COMPLETED,       // Step succeeded (consumer acknowledged or synchronous OK)
    FAILED,          // Step forward execution failed after retries
    COMPENSATED,     // Compensating step succeeded
    COMPENSATION_FAILED  // Compensating step failed - saga becomes FAILED
}
```

- [ ] **Step 4: Create SagaType enum**

Write to `order-service/src/main/java/com/pcms/orderservice/saga/SagaType.java`:

```java
package com.pcms.orderservice.saga;

/**
 * Type of distributed transaction orchestrated by the saga.
 * Each type has its own predefined step sequence.
 */
public enum SagaType {
    ORDER_FULFILLMENT   // Stock consume → points award → notification
}
```

- [ ] **Step 5: Verify file creation**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ls order-service/src/main/java/com/pcms/orderservice/saga/`
Expected: Shows 3 files

---

## Task 2: Create SagaInstance entity

**Files:**
- Create: `order-service/src/main/java/com/pcms/orderservice/entity/SagaInstance.java`

- [ ] **Step 1: Create SagaInstance entity**

Write to `order-service/src/main/java/com/pcms/orderservice/entity/SagaInstance.java`:

```java
package com.pcms.orderservice.entity;

import com.pcms.common.entity.BaseEntity;
import com.pcms.orderservice.saga.SagaStatus;
import com.pcms.orderservice.saga.SagaType;
import jakarta.persistence.*;

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
public class SagaInstance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
```

- [ ] **Step 2: Verify file**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && head -20 order-service/src/main/java/com/pcms/orderservice/entity/SagaInstance.java`
Expected: Shows the entity class declaration

---

## Task 3: Create SagaStep entity

**Files:**
- Create: `order-service/src/main/java/com/pcms/orderservice/entity/SagaStep.java`

- [ ] **Step 1: Create SagaStep entity**

Write to `order-service/src/main/java/com/pcms/orderservice/entity/SagaStep.java`:

```java
package com.pcms.orderservice.entity;

import com.pcms.common.entity.BaseEntity;
import com.pcms.orderservice.saga.SagaStepStatus;
import jakarta.persistence.*;

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
public class SagaStep extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
```

- [ ] **Step 2: Verify file**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && grep "class SagaStep" order-service/src/main/java/com/pcms/orderservice/entity/SagaStep.java`
Expected: `public class SagaStep extends BaseEntity {`

---

## Task 4: Create Saga repositories

**Files:**
- Create: `order-service/src/main/java/com/pcms/orderservice/repository/SagaInstanceRepository.java`
- Create: `order-service/src/main/java/com/pcms/orderservice/repository/SagaStepRepository.java`

- [ ] **Step 1: Create SagaInstanceRepository**

Write to `order-service/src/main/java/com/pcms/orderservice/repository/SagaInstanceRepository.java`:

```java
package com.pcms.orderservice.repository;

import com.pcms.orderservice.entity.SagaInstance;
import com.pcms.orderservice.saga.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SagaInstanceRepository extends JpaRepository<SagaInstance, UUID> {

    Optional<SagaInstance> findByAggregateTypeAndAggregateId(String aggregateType, UUID aggregateId);

    @Query("""
            SELECT s FROM SagaInstance s
            WHERE s.status IN :statuses
              AND s.startedAt < :threshold
            """)
    List<SagaInstance> findStuckSagas(List<SagaStatus> statuses, LocalDateTime threshold);

    List<SagaInstance> findByStatusIn(List<SagaStatus> statuses);
}
```

- [ ] **Step 2: Create SagaStepRepository**

Write to `order-service/src/main/java/com/pcms/orderservice/repository/SagaStepRepository.java`:

```java
package com.pcms.orderservice.repository;

import com.pcms.orderservice.entity.SagaStep;
import com.pcms.orderservice.saga.SagaStepStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SagaStepRepository extends JpaRepository<SagaStep, UUID> {

    List<SagaStep> findBySagaIdOrderByStepOrderAsc(UUID sagaId);

    List<SagaStep> findBySagaIdAndStatus(UUID sagaId, SagaStepStatus status);

    List<SagaStep> findBySagaIdAndCompensation(UUID sagaId, Boolean compensation);
}
```

- [ ] **Step 3: Verify repositories**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ls order-service/src/main/java/com/pcms/orderservice/repository/Saga*`
Expected: 2 files

---

## Task 5: Add bulk DTOs to inventory-service

**Files:**
- Create: `inventory-service/src/main/java/com/pcms/inventoryservice/dto/BulkConsumeRequest.java`
- Create: `inventory-service/src/main/java/com/pcms/inventoryservice/dto/BulkRestoreRequest.java`

- [ ] **Step 1: Create BulkConsumeRequest**

Write to `inventory-service/src/main/java/com/pcms/inventoryservice/dto/BulkConsumeRequest.java`:

```java
package com.pcms.inventoryservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Bulk stock-consume payload for saga orchestrator.
 * The orderId on each item identifies the saga aggregate.
 */
public record BulkConsumeRequest(
        @NotEmpty @Valid List<ConsumeBatchRequest> items
) {}
```

- [ ] **Step 2: Create BulkRestoreRequest**

Write to `inventory-service/src/main/java/com/pcms/inventoryservice/dto/BulkRestoreRequest.java`:

```java
package com.pcms.inventoryservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Bulk stock-restore payload for saga compensation.
 * Items must match the original consume: same medicineId/branchId/qty,
 * looked up via InventoryTransaction.refId.
 */
public record BulkRestoreRequest(
        @NotEmpty @Valid List<ConsumeBatchRequest> items
) {}
```

---

## Task 6: Add bulk consumeStock and precise restoreStock to inventory-service

**Files:**
- Modify: `inventory-service/src/main/java/com/pcms/inventoryservice/service/impl/InventoryServiceImpl.java`

- [ ] **Step 1: Check existing imports**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && head -25 inventory-service/src/main/java/com/pcms/inventoryservice/service/impl/InventoryServiceImpl.java`
Expected: Shows imports

- [ ] **Step 2: Add new imports for bulk operations**

Find the imports block and add the following (after existing inventory imports):
```java
import com.pcms.inventoryservice.dto.BulkConsumeRequest;
import com.pcms.inventoryservice.dto.BulkRestoreRequest;
import com.pcms.inventoryservice.entity.InventoryTransaction;
import com.pcms.inventoryservice.repository.InventoryTransactionRepository;
import java.util.ArrayList;
```

- [ ] **Step 3: Add bulkConsumeStock method**

Add the following method to InventoryServiceImpl class (anywhere after the existing methods):

```java
@Override
@Transactional
public List<StockOperationResult> bulkConsumeStock(BulkConsumeRequest request) {
    List<StockOperationResult> results = new ArrayList<>();
    for (ConsumeBatchRequest item : request.items()) {
        StockOperationResult result = consumeStock(item);
        results.add(result);
    }
    return results;
}
```

- [ ] **Step 4: Add precise restoreStockByOrder method (compensation)**

Add the following method:

```java
@Override
@Transactional
public List<StockOperationResult> restoreStockByOrder(UUID orderId) {
    // Look up all InventoryTransaction rows where refId = orderId AND type = SALE
    List<InventoryTransaction> consumeTxns = txnRepository.findByRefIdAndType(orderId, TransactionType.SALE);
    List<StockOperationResult> results = new ArrayList<>();
    for (InventoryTransaction txn : consumeTxns) {
        ConsumeBatchRequest restoreReq = new ConsumeBatchRequest(
                txn.getMedicineId(), txn.getBranchId(), txn.getQty(),
                txn.getActorId(), orderId);
        StockOperationResult result = restoreStock(restoreReq);
        results.add(result);
    }
    return results;
}
```

- [ ] **Step 5: Add bulkRestoreStock method**

Add the following method:

```java
@Override
@Transactional
public List<StockOperationResult> bulkRestoreStock(BulkRestoreRequest request) {
    List<StockOperationResult> results = new ArrayList<>();
    for (ConsumeBatchRequest item : request.items()) {
        StockOperationResult result = restoreStock(item);
        results.add(result);
    }
    return results;
}
```

- [ ] **Step 6: Verify build**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn compile -pl inventory-service 2>&1 | tail -5`
Expected: `BUILD SUCCESS`

---

## Task 7: Update InventoryOutboxConsumerController to accept bulk endpoints

**Files:**
- Modify: `inventory-service/src/main/java/com/pcms/inventoryservice/controller/OutboxConsumerController.java`

- [ ] **Step 1: Read existing controller**

Run: `cat "C:/Users/ADMIN/Downloads/temp_v12/pcms/inventory-service/src/main/java/com/pcms/inventoryservice/controller/OutboxConsumerController.java"`
Expected: Shows existing 2 endpoints

- [ ] **Step 2: Add bulk consume endpoint**

After the existing `orderPaid` method, add:

```java
    @PostMapping("/{orderId}/paid-bulk")
    public ResponseEntity<?> orderPaidBulk(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Outbox-Event-Id", required = false) UUID eventId,
            @Valid @RequestBody BulkConsumeRequest request) {
        return ResponseEntity.ok(outboxConsumerService.handleOrderPaidBulk(orderId, eventId, request));
    }
```

- [ ] **Step 3: Add bulk restore endpoint**

After `orderPaidBulk`, add:

```java
    @PostMapping("/{orderId}/cancelled-bulk")
    public ResponseEntity<?> orderCancelledBulk(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Outbox-Event-Id", required = false) UUID eventId,
            @Valid @RequestBody BulkRestoreRequest request) {
        return ResponseEntity.ok(outboxConsumerService.handleOrderCancelledBulk(orderId, eventId, request));
    }
```

- [ ] **Step 4: Add precise restore endpoint**

Add:

```java
    @PostMapping("/{orderId}/cancelled-precise")
    public ResponseEntity<?> orderCancelledPrecise(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Outbox-Event-Id", required = false) UUID eventId) {
        return ResponseEntity.ok(outboxConsumerService.handleOrderCancelledPrecise(orderId, eventId));
    }
```

---

## Task 8: Add bulk handlers to OutboxConsumerServiceImpl

**Files:**
- Modify: `inventory-service/src/main/java/com/pcms/inventoryservice/service/impl/OutboxConsumerServiceImpl.java`

- [ ] **Step 1: Read existing service**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && head -30 inventory-service/src/main/java/com/pcms/inventoryservice/service/impl/OutboxConsumerServiceImpl.java`
Expected: Shows imports

- [ ] **Step 2: Add bulk method to interface**

Add the following methods to the OutboxConsumerService interface (create file or edit if existing):

```java
    Object handleOrderPaidBulk(UUID orderId, UUID eventId, BulkConsumeRequest request);
    Object handleOrderCancelledBulk(UUID orderId, UUID eventId, BulkRestoreRequest request);
    Object handleOrderCancelledPrecise(UUID orderId, UUID eventId);
```

- [ ] **Step 3: Implement bulk handlers**

Add to OutboxConsumerServiceImpl class:

```java
    @Override
    public Object handleOrderPaidBulk(UUID orderId, UUID eventId, BulkConsumeRequest request) {
        UUID effectiveEventId = eventId != null ? eventId : orderId;
        if (outboxLogRepository.existsByEventId(effectiveEventId)) {
            return Map.of("status", "duplicate", "eventId", effectiveEventId);
        }
        List<StockOperationResult> results = inventoryService.bulkConsumeStock(request);
        outboxLogRepository.save(new OutboxLog(effectiveEventId, "ORDER_PAID_STOCK_CONSUME_BULK", "PROCESSED", orderId));
        return Map.of("status", "processed", "results", results);
    }

    @Override
    public Object handleOrderCancelledBulk(UUID orderId, UUID eventId, BulkRestoreRequest request) {
        UUID effectiveEventId = eventId != null ? eventId : orderId;
        if (outboxLogRepository.existsByEventId(effectiveEventId)) {
            return Map.of("status", "duplicate", "eventId", effectiveEventId);
        }
        List<StockOperationResult> results = inventoryService.bulkRestoreStock(request);
        outboxLogRepository.save(new OutboxLog(effectiveEventId, "ORDER_CANCELLED_STOCK_RESTORE_BULK", "PROCESSED", orderId));
        return Map.of("status", "processed", "results", results);
    }

    @Override
    public Object handleOrderCancelledPrecise(UUID orderId, UUID eventId) {
        UUID effectiveEventId = eventId != null ? eventId : orderId;
        if (outboxLogRepository.existsByEventId(effectiveEventId)) {
            return Map.of("status", "duplicate", "eventId", effectiveEventId);
        }
        List<StockOperationResult> results = inventoryService.restoreStockByOrder(orderId);
        outboxLogRepository.save(new OutboxLog(effectiveEventId, "ORDER_CANCELLED_STOCK_RESTORE_PRECISE", "PROCESSED", orderId));
        return Map.of("status", "processed", "results", results);
    }
```

- [ ] **Step 4: Verify build**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn compile -pl inventory-service 2>&1 | tail -5`
Expected: `BUILD SUCCESS`

---

## Task 9: Create SagaOrchestrator service

**Files:**
- Create: `order-service/src/main/java/com/pcms/orderservice/saga/SagaOrchestrator.java`

- [ ] **Step 1: Create SagaOrchestrator**

Write to `order-service/src/main/java/com/pcms/orderservice/saga/SagaOrchestrator.java`:

```java
package com.pcms.orderservice.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcms.orderservice.entity.Order;
import com.pcms.orderservice.entity.OrderItem;
import com.pcms.orderservice.entity.OutboxEvent;
import com.pcms.orderservice.entity.SagaInstance;
import com.pcms.orderservice.entity.SagaStep;
import com.pcms.orderservice.repository.OutboxEventRepository;
import com.pcms.orderservice.repository.SagaInstanceRepository;
import com.pcms.orderservice.repository.SagaStepRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Central saga orchestrator.
 *
 * <p>Defines the step sequence for each SagaType, persists the SagaInstance
 * and its SagaSteps in one transaction, then emits OutboxEvents for each step.
 *
 * <p>Compensation logic is delegated to {@link SagaCompensationHandler}.
 * Step execution status is monitored by {@code SagaTimeoutScheduler}.
 *
 * <p>B-17: Saga pattern - orchestration flavor.
 */
@Service
public class SagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SagaOrchestrator.class);

    private final SagaInstanceRepository sagaRepo;
    private final SagaStepRepository stepRepo;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    public SagaOrchestrator(SagaInstanceRepository sagaRepo, SagaStepRepository stepRepo,
            OutboxEventRepository outboxRepo, ObjectMapper objectMapper) {
        this.sagaRepo = sagaRepo;
        this.stepRepo = stepRepo;
        this.outboxRepo = outboxRepo;
        this.objectMapper = objectMapper;
    }

    /**
     * Start a new Order Fulfillment Saga. Persists the saga and all forward
     * steps + their compensations, then emits one OutboxEvent per forward step.
     */
    @Transactional
    public SagaInstance startOrderFulfillment(Order order, UUID actorId) {
        // Idempotency: skip if a saga for this order already exists and is not terminal
        var existing = sagaRepo.findByAggregateTypeAndAggregateId("Order", order.getId());
        if (existing.isPresent() && existing.get().getStatus() != SagaStatus.COMPENSATED
                && existing.get().getStatus() != SagaStatus.FAILED) {
            log.info("[saga] Saga already exists for order {}, status={}", order.getId(), existing.get().getStatus());
            return existing.get();
        }

        SagaInstance saga = new SagaInstance();
        saga.setSagaType(SagaType.ORDER_FULFILLMENT);
        saga.setStatus(SagaStatus.STARTED);
        saga.setAggregateType("Order");
        saga.setAggregateId(order.getId());
        saga.setCorrelationId(UUID.randomUUID().toString());
        saga.setStartedAt(LocalDateTime.now());
        saga = sagaRepo.save(saga);

        int stepOrder = 1;
        // Forward step 1: stock consume (bulk)
        StringBuilder itemsJson = new StringBuilder("[");
        for (OrderItem item : order.getItems()) {
            if (itemsJson.length() > 1) itemsJson.append(",");
            itemsJson.append(String.format(
                    "{\"medicineId\":\"%s\",\"branchId\":\"%s\",\"qty\":%d,\"actorId\":\"%s\",\"orderId\":\"%s\"}",
                    item.getMedicineId(), order.getBranchId(), item.getQty(), actorId, order.getId()));
        }
        itemsJson.append("]");

        SagaStep stockStep = createStep(saga, stepOrder++, "STOCK_CONSUMED",
                "inventory-service", "/inventory/orders/" + order.getId() + "/paid-bulk",
                "{\"items\":" + itemsJson + "}", false, null);

        // Forward step 2: points award
        int points = order.getTotal().divide(java.math.BigDecimal.valueOf(1000), 0, java.math.RoundingMode.FLOOR).intValue();
        if (points > 0) {
            SagaStep pointsStep = createStep(saga, stepOrder++, "POINTS_AWARDED",
                    "customer-service", "/customers/" + order.getCustomerId() + "/points/add",
                    String.format("{\"points\":%d,\"refOrderId\":\"%s\",\"reason\":\"ORDER_PAID:%s\"}",
                            points, order.getId(), order.getOrderNumber()),
                    false, null);
            registerCompensation(pointsStep, "POINTS_REVERSAL",
                    "customer-service", "/customers/" + order.getCustomerId() + "/points/reverse",
                    String.format("{\"points\":%d,\"refOrderId\":\"%s\",\"reason\":\"ORDER_CANCELLED:%s\"}",
                            points, order.getId(), order.getOrderNumber()));
        }

        // Forward step 3: notification
        SagaStep notificationStep = createStep(saga, stepOrder++, "ORDER_PAID_NOTIFICATION",
                "notification-service", "/notifications/orders/paid",
                String.format(
                        "{\"orderId\":\"%s\",\"orderNumber\":\"%s\",\"customerId\":\"%s\",\"branchId\":\"%s\",\"staffId\":\"%s\",\"total\":%s}",
                        order.getId(), order.getOrderNumber(), order.getCustomerId(), order.getBranchId(),
                        actorId, order.getTotal()),
                false, null);
        registerCompensation(notificationStep, "ORDER_CANCELLED_NOTIFICATION",
                "notification-service", "/notifications/orders/cancelled",
                String.format("{\"orderId\":\"%s\",\"orderNumber\":\"%s\"}", order.getId(), order.getOrderNumber()));

        saga.setStatus(SagaStatus.IN_PROGRESS);
        saga = sagaRepo.save(saga);

        // Emit OutboxEvent for each forward step
        for (SagaStep step : saga.getSteps()) {
            if (Boolean.FALSE.equals(step.getCompensation())) {
                OutboxEvent event = new OutboxEvent(
                        "SagaInstance", saga.getId(), step.getStepName(),
                        step.getTargetService(), step.getEndpoint(), step.getPayload());
                event = outboxRepo.save(event);
                step.setOutboxEventId(event.getId());
                step.setStatus(SagaStepStatus.IN_PROGRESS);
                step.setExecutedAt(LocalDateTime.now());
                stepRepo.save(step);
            }
        }

        log.info("[saga] Started Order Fulfillment saga={} for order={} with {} steps",
                saga.getId(), order.getId(), saga.getSteps().size());
        return saga;
    }

    private SagaStep createStep(SagaInstance saga, int order, String name, String targetService,
            String endpoint, String payload, boolean compensation, UUID compensatesStepId) {
        SagaStep step = new SagaStep();
        step.setSaga(saga);
        step.setStepOrder(order);
        step.setStepName(name);
        step.setTargetService(targetService);
        step.setEndpoint(endpoint);
        step.setPayload(payload);
        step.setCompensation(compensation);
        step.setCompensatesStepId(compensatesStepId);
        step.setStatus(SagaStepStatus.PENDING);
        return stepRepo.save(step);
    }

    private void registerCompensation(SagaStep forwardStep, String compName,
            String targetService, String endpoint, String payload) {
        SagaInstance saga = forwardStep.getSaga();
        SagaStep comp = createStep(saga, forwardStep.getStepOrder() + 1000, compName,
                targetService, endpoint, payload, true, forwardStep.getId());
        // Set compensates reference - reverse step points to forward step
        comp.setCompensatesStepId(forwardStep.getId());
        stepRepo.save(comp);
    }
}
```

- [ ] **Step 2: Verify compile**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn compile -pl order-service 2>&1 | tail -5`
Expected: `BUILD SUCCESS`

---

## Task 10: Create SagaCompensationHandler

**Files:**
- Create: `order-service/src/main/java/com/pcms/orderservice/saga/SagaCompensationHandler.java`

- [ ] **Step 1: Create SagaCompensationHandler**

Write to `order-service/src/main/java/com/pcms/orderservice/saga/SagaCompensationHandler.java`:

```java
package com.pcms.orderservice.saga;

import com.pcms.orderservice.entity.SagaInstance;
import com.pcms.orderservice.entity.SagaStep;
import com.pcms.orderservice.repository.SagaInstanceRepository;
import com.pcms.orderservice.repository.SagaStepRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs compensation steps for a failed saga.
 *
 * <p>Order of compensation: REVERSE order of forward steps.
 * Each compensation step emits an OutboxEvent with its target endpoint.
 *
 * <p>B-17: Saga pattern - automatic compensation on failure.
 */
@Service
public class SagaCompensationHandler {

    private static final Logger log = LoggerFactory.getLogger(SagaCompensationHandler.class);

    private final SagaInstanceRepository sagaRepo;
    private final SagaStepRepository stepRepo;
    private final com.pcms.orderservice.repository.OutboxEventRepository outboxRepo;

    public SagaCompensationHandler(SagaInstanceRepository sagaRepo, SagaStepRepository stepRepo,
            com.pcms.orderservice.repository.OutboxEventRepository outboxRepo) {
        this.sagaRepo = sagaRepo;
        this.stepRepo = stepRepo;
        this.outboxRepo = outboxRepo;
    }

    /**
     * Trigger compensation for a failed saga. Marks saga COMPENSATING,
     * emits outbox events for each compensation step.
     */
    @Transactional
    public void compensate(UUID sagaId, String reason) {
        SagaInstance saga = sagaRepo.findById(sagaId)
                .orElseThrow(() -> new IllegalStateException("Saga not found: " + sagaId));
        if (saga.getStatus() != SagaStatus.IN_PROGRESS && saga.getStatus() != SagaStatus.COMPENSATING) {
            log.warn("[saga] Cannot compensate saga {} - invalid status {}", sagaId, saga.getStatus());
            return;
        }
        saga.setStatus(SagaStatus.COMPENSATING);
        saga.setLastError(reason);
        sagaRepo.save(saga);

        // Get compensation steps in reverse order
        List<SagaStep> compensations = stepRepo.findBySagaIdAndCompensation(sagaId, true);
        compensations.sort((a, b) -> b.getStepOrder() - a.getStepOrder());

        for (SagaStep comp : compensations) {
            try {
                com.pcms.orderservice.entity.OutboxEvent event = new com.pcms.orderservice.entity.OutboxEvent(
                        "SagaInstance", sagaId, comp.getStepName(),
                        comp.getTargetService(), comp.getEndpoint(), comp.getPayload());
                event = outboxRepo.save(event);
                comp.setOutboxEventId(event.getId());
                comp.setStatus(SagaStepStatus.IN_PROGRESS);
                comp.setExecutedAt(LocalDateTime.now());
                stepRepo.save(comp);
                log.info("[saga] Saga {} compensation step {} ({}) queued as outbox event {}",
                        sagaId, comp.getStepOrder(), comp.getStepName(), event.getId());
            } catch (Exception e) {
                comp.setStatus(SagaStepStatus.COMPENSATION_FAILED);
                comp.setErrorMessage(e.getMessage());
                stepRepo.save(comp);
                log.error("[saga] Saga {} compensation step {} failed to queue: {}",
                        sagaId, comp.getStepName(), e.getMessage());
            }
        }
        saga.setCompletedAt(LocalDateTime.now());
        sagaRepo.save(saga);
    }

    /**
     * Mark a compensation step as completed (called by consumer callback or
     * OutboxPublisher when the compensating event is delivered).
     */
    @Transactional
    public void markStepCompleted(UUID sagaStepId) {
        SagaStep step = stepRepo.findById(sagaStepId).orElse(null);
        if (step == null) return;
        step.setStatus(SagaStepStatus.COMPENSATED);
        step.setCompletedAt(LocalDateTime.now());
        stepRepo.save(step);
    }

    /**
     * Mark a saga as fully COMPENSATED (all compensation steps done).
     */
    @Transactional
    public void markSagaCompensated(UUID sagaId) {
        SagaInstance saga = sagaRepo.findById(sagaId).orElse(null);
        if (saga == null) return;
        if (saga.getStatus() == SagaStatus.COMPENSATING) {
            saga.setStatus(SagaStatus.COMPENSATED);
            sagaRepo.save(saga);
        }
    }
}
```

- [ ] **Step 2: Verify compile**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn compile -pl order-service 2>&1 | tail -5`
Expected: `BUILD SUCCESS`

---

## Task 11: Create SagaTimeoutScheduler

**Files:**
- Create: `order-service/src/main/java/com/pcms/orderservice/scheduler/SagaTimeoutScheduler.java`

- [ ] **Step 1: Create the scheduler**

Write to `order-service/src/main/java/com/pcms/orderservice/scheduler/SagaTimeoutScheduler.java`:

```java
package com.pcms.orderservice.scheduler;

import com.pcms.orderservice.entity.SagaInstance;
import com.pcms.orderservice.entity.SagaStep;
import com.pcms.orderservice.repository.SagaInstanceRepository;
import com.pcms.orderservice.repository.SagaStepRepository;
import com.pcms.orderservice.saga.SagaCompensationHandler;
import com.pcms.orderservice.saga.SagaStatus;
import com.pcms.orderservice.saga.SagaStepStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Detects and recovers stuck sagas.
 *
 * <p>Stuck saga = IN_PROGRESS for more than {@code SAGA_TIMEOUT_MINUTES}
 * with at least one step in PENDING/IN_PROGRESS.
 *
 * <p>B-17: Saga pattern - timeout/reaper.
 */
@Component
public class SagaTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(SagaTimeoutScheduler.class);
    private static final int SAGA_TIMEOUT_MINUTES = 30;

    private final SagaInstanceRepository sagaRepo;
    private final SagaStepRepository stepRepo;
    private final SagaCompensationHandler compensationHandler;

    public SagaTimeoutScheduler(SagaInstanceRepository sagaRepo, SagaStepRepository stepRepo,
            SagaCompensationHandler compensationHandler) {
        this.sagaRepo = sagaRepo;
        this.stepRepo = stepRepo;
        this.compensationHandler = compensationHandler;
    }

    @Scheduled(fixedRate = 300_000) // every 5 minutes
    @Transactional
    public void detectStuckSagas() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(SAGA_TIMEOUT_MINUTES);
        List<SagaStatus> activeStatuses = List.of(SagaStatus.STARTED, SagaStatus.IN_PROGRESS);
        List<SagaInstance> stuckSagas = sagaRepo.findStuckSagas(activeStatuses, threshold);
        for (SagaInstance saga : stuckSagas) {
            log.warn("[saga] Detected stuck saga {} for {} {} - started {}",
                    saga.getId(), saga.getAggregateType(), saga.getAggregateId(), saga.getStartedAt());
            // Check if any step is still pending/in-progress
            List<SagaStep> inProgressSteps = stepRepo.findBySagaIdAndStatus(saga.getId(),
                    SagaStepStatus.IN_PROGRESS);
            if (!inProgressSteps.isEmpty()) {
                // Wait for outbox retry - skip for now
                log.info("[saga] Saga {} has {} in-progress steps, waiting", saga.getId(), inProgressSteps.size());
                continue;
            }
            List<SagaStep> pendingSteps = stepRepo.findBySagaIdAndStatus(saga.getId(),
                    SagaStepStatus.PENDING);
            if (!pendingSteps.isEmpty()) {
                log.warn("[saga] Saga {} has {} pending steps - triggering compensation", saga.getId(), pendingSteps.size());
                compensationHandler.compensate(saga.getId(), "Saga timeout: pending steps not executed in " + SAGA_TIMEOUT_MINUTES + " min");
            }
        }
    }
}
```

---

## Task 12: Create SagaStepExecutor (process step completion callbacks)

**Files:**
- Create: `order-service/src/main/java/com/pcms/orderservice/saga/SagaStepExecutor.java`

- [ ] **Step 1: Create the executor**

Write to `order-service/src/main/java/com/pcms/orderservice/saga/SagaStepExecutor.java`:

```java
package com.pcms.orderservice.saga;

import com.pcms.orderservice.entity.SagaInstance;
import com.pcms.orderservice.entity.SagaStep;
import com.pcms.orderservice.repository.SagaInstanceRepository;
import com.pcms.orderservice.repository.SagaStepRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Marks saga steps as completed/failed and triggers compensation on failure.
 *
 * <p>Called by the OutboxPublisher when an event is delivered (success/failure).
 * Tracks each step's progress and decides if the saga should continue, compensate,
 * or complete.
 *
 * <p>B-17: Saga pattern - step execution tracking.
 */
@Service
public class SagaStepExecutor {

    private static final Logger log = LoggerFactory.getLogger(SagaStepExecutor.class);

    private final SagaInstanceRepository sagaRepo;
    private final SagaStepRepository stepRepo;
    private final SagaCompensationHandler compensationHandler;

    public SagaStepExecutor(SagaInstanceRepository sagaRepo, SagaStepRepository stepRepo,
            SagaCompensationHandler compensationHandler) {
        this.sagaRepo = sagaRepo;
        this.stepRepo = stepRepo;
        this.compensationHandler = compensationHandler;
    }

    @Transactional
    public void onStepSuccess(UUID sagaStepId) {
        SagaStep step = stepRepo.findById(sagaStepId).orElse(null);
        if (step == null) return;
        step.setStatus(SagaStepStatus.COMPLETED);
        step.setCompletedAt(LocalDateTime.now());
        stepRepo.save(step);
        log.info("[saga] Step {} ({}) completed", sagaStepId, step.getStepName());
        checkSagaCompletion(step.getSaga().getId());
    }

    @Transactional
    public void onStepFailure(UUID sagaStepId, String errorMessage) {
        SagaStep step = stepRepo.findById(sagaStepId).orElse(null);
        if (step == null) return;
        step.setStatus(SagaStepStatus.FAILED);
        step.setErrorMessage(errorMessage);
        step.setRetryCount(step.getRetryCount() + 1);
        stepRepo.save(step);
        log.error("[saga] Step {} ({}) FAILED: {}", sagaStepId, step.getStepName(), errorMessage);
        // Trigger compensation
        compensationHandler.compensate(step.getSaga().getId(),
                "Step " + step.getStepName() + " failed: " + errorMessage);
    }

    private void checkSagaCompletion(UUID sagaId) {
        SagaInstance saga = sagaRepo.findById(sagaId).orElse(null);
        if (saga == null) return;
        // If all forward steps are COMPLETED -> saga COMPLETED
        boolean allForwardCompleted = saga.getSteps().stream()
                .filter(s -> Boolean.FALSE.equals(s.getCompensation()))
                .allMatch(s -> s.getStatus() == SagaStepStatus.COMPLETED);
        if (allForwardCompleted && saga.getStatus() == SagaStatus.IN_PROGRESS) {
            saga.setStatus(SagaStatus.COMPLETED);
            saga.setCompletedAt(LocalDateTime.now());
            sagaRepo.save(saga);
            log.info("[saga] Saga {} COMPLETED", sagaId);
        }
    }
}
```

---

## Task 13: Wire OrderServiceImpl.markAsPaid to start the saga

**Files:**
- Modify: `order-service/src/main/java/com/pcms/orderservice/service/impl/OrderServiceImpl.java`

- [ ] **Step 1: Add SagaOrchestrator dependency**

In `OrderServiceImpl.java`, add the new field and constructor injection:

```java
    private final SagaOrchestrator sagaOrchestrator;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            CatalogClient catalogClient,
            InventoryClient inventoryClient,
            CustomerClient customerClient,
            PrescriptionClient prescriptionClient,
            BranchClient branchClient,
            OrderSequenceRepository sequenceRepository,
            OutboxEventRepository outboxRepo,
            CouponService couponService,
            SagaOrchestrator sagaOrchestrator) {
        // ... existing assignments
        this.sagaOrchestrator = sagaOrchestrator;
    }
```

- [ ] **Step 2: Modify markAsPaid to call saga instead of writing 3 separate outbox events**

Replace the body of `markAsPaid()` (after the status save and before the outbox loop) with:

```java
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // B-17: Start orchestrated saga instead of 3 separate outbox events
        sagaOrchestrator.startOrderFulfillment(order, actorId);

        return OrderResponse.from(order);
```

- [ ] **Step 3: Verify build**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn compile -pl order-service 2>&1 | tail -5`
Expected: `BUILD SUCCESS`

---

## Task 14: Update OrderServiceImpl.cancel to trigger saga compensation

**Files:**
- Modify: `order-service/src/main/java/com/pcms/orderservice/service/impl/OrderServiceImpl.java`

- [ ] **Step 1: Add SagaCompensationHandler dependency**

In `OrderServiceImpl.java`, add new field and constructor parameter:

```java
    private final SagaCompensationHandler sagaCompensationHandler;

    // add to constructor:
    this.sagaCompensationHandler = sagaCompensationHandler;
```

- [ ] **Step 2: Modify cancel to trigger compensation**

Replace the cancel() method body (after status save) with:

```java
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        // B-17: Trigger saga compensation if a saga exists for this order
        var saga = sagaRepo.findByAggregateTypeAndAggregateId("Order", orderId).orElse(null);
        if (saga.isPresent()) {
            sagaCompensationHandler.compensate(saga.get().getId(),
                    "Manual cancellation by user/actor " + actorId);
        } else {
            // Backwards-compat: keep the original STOCK_RESTORED outbox event for old sagas
            if (previousStatus == OrderStatus.PAID) {
                for (OrderItem item : order.getItems()) {
                    String payload = String.format(
                            "{\"medicineId\":\"%s\",\"branchId\":\"%s\",\"qty\":%d,\"orderId\":\"%s\",\"actorId\":\"%s\"}",
                            item.getMedicineId(), order.getBranchId(), item.getQty(), order.getId(), actorId);
                    outboxRepo.save(new OutboxEvent(
                            "Order", order.getId(), "STOCK_RESTORED",
                            "inventory-service", "/inventory/orders/" + order.getId() + "/cancelled", payload));
                }
            }
        }
        return OrderResponse.from(saved);
```

- [ ] **Step 3: Verify build**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn package -DskipTests -pl order-service 2>&1 | tail -3`
Expected: `BUILD SUCCESS`

---

## Task 15: Create SagaAdminController for inspection

**Files:**
- Create: `order-service/src/main/java/com/pcms/orderservice/saga/controller/SagaAdminController.java`

- [ ] **Step 1: Create admin controller**

Write to `order-service/src/main/java/com/pcms/orderservice/saga/controller/SagaAdminController.java`:

```java
package com.pcms.orderservice.saga.controller;

import com.pcms.orderservice.entity.SagaInstance;
import com.pcms.orderservice.entity.SagaStep;
import com.pcms.orderservice.repository.SagaInstanceRepository;
import com.pcms.orderservice.repository.SagaStepRepository;
import com.pcms.orderservice.saga.SagaCompensationHandler;
import com.pcms.orderservice.saga.SagaStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin endpoints to inspect and manually trigger saga actions.
 */
@RestController
@RequestMapping("/admin/saga")
public class SagaAdminController {

    private final SagaInstanceRepository sagaRepo;
    private final SagaStepRepository stepRepo;
    private final SagaCompensationHandler compensationHandler;

    public SagaAdminController(SagaInstanceRepository sagaRepo, SagaStepRepository stepRepo,
            SagaCompensationHandler compensationHandler) {
        this.sagaRepo = sagaRepo;
        this.stepRepo = stepRepo;
        this.compensationHandler = compensationHandler;
    }

    @GetMapping("/{sagaId}")
    public ResponseEntity<?> getSaga(@PathVariable UUID sagaId) {
        SagaInstance saga = sagaRepo.findById(sagaId).orElse(null);
        if (saga == null) return ResponseEntity.notFound().build();
        List<SagaStep> steps = stepRepo.findBySagaIdOrderByStepOrderAsc(sagaId);
        return ResponseEntity.ok(Map.of("saga", saga, "steps", steps));
    }

    @GetMapping("/by-aggregate/{aggregateType}/{aggregateId}")
    public ResponseEntity<?> getSagaByAggregate(@PathVariable String aggregateType,
            @PathVariable UUID aggregateId) {
        SagaInstance saga = sagaRepo.findByAggregateTypeAndAggregateId(aggregateType, aggregateId).orElse(null);
        if (saga == null) return ResponseEntity.notFound().build();
        List<SagaStep> steps = stepRepo.findBySagaIdOrderByStepOrderAsc(saga.getId());
        return ResponseEntity.ok(Map.of("saga", saga, "steps", steps));
    }

    @PostMapping("/{sagaId}/compensate")
    public ResponseEntity<?> triggerCompensation(@PathVariable UUID sagaId,
            @RequestParam(defaultValue = "Manual trigger") String reason) {
        compensationHandler.compensate(sagaId, reason);
        return ResponseEntity.ok(Map.of("status", "compensation_triggered", "sagaId", sagaId));
    }

    @GetMapping("/stuck")
    public ResponseEntity<?> getStuckSagas() {
        return ResponseEntity.ok(Map.of(
                "sagas", sagaRepo.findStuckSagas(
                        List.of(SagaStatus.STARTED, SagaStatus.IN_PROGRESS),
                        java.time.LocalDateTime.now().minusMinutes(30))));
    }
}
```

---

## Task 16: Wire PaymentServiceImpl to trigger saga via event

**Files:**
- Modify: `payment-service/src/main/java/com/pcms/paymentservice/service/impl/PaymentServiceImpl.java`

- [ ] **Step 1: Replace synchronous orderClient call with OutboxEvent**

In `PaymentServiceImpl.create()`, replace the try-catch block that calls `orderClient.markOrderPaid(...)`:

```java
        // B-17: Emit OutboxEvent to trigger saga (replaces direct orderClient call)
        try {
            outboxRepo.save(new com.pcms.paymentservice.entity.OutboxEvent(
                    null, "PAYMENT_COMPLETED",
                    "order-service", "/orders/" + request.orderId() + "/saga/start",
                    String.format("{\"orderId\":\"%s\",\"paymentId\":\"%s\",\"actorId\":\"%s\"}",
                            request.orderId(), saved.getId(), request.staffId())));
        } catch (Exception e) {
            log.warn("Failed to emit payment-completed outbox event for order {}: {}",
                    request.orderId(), e.getMessage());
        }
```

---

## Task 17: Wire OutboxPublisher to notify SagaStepExecutor

**Files:**
- Modify: `order-service/src/main/java/com/pcms/orderservice/scheduler/OutboxPublisher.java`

- [ ] **Step 1: Add SagaStepExecutor dependency**

Add field and constructor parameter:

```java
    private final SagaStepExecutor sagaStepExecutor;

    // add to constructor
    this.sagaStepExecutor = sagaStepExecutor;
```

- [ ] **Step 2: Call sagaStepExecutor on success**

In `publishPending()`, replace the success block (after `event.setStatus(OutboxEvent.Status.SENT);`) with:

```java
                event.setStatus(OutboxEvent.Status.SENT);
                event.setSentAt(LocalDateTime.now());
                outboxRepo.save(event);
                // B-17: Notify saga executor on successful delivery
                if (event.getOutboxEventId() != null) {
                    // find the saga step via outbox event id and mark success
                    sagaStepExecutor.onStepSuccess(event.getOutboxEventId());
                }
```

- [ ] **Step 3: Verify build**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn package -DskipTests -pl order-service 2>&1 | tail -3`
Expected: `BUILD SUCCESS`

---

## Task 18: Verify build, restart, and test end-to-end

**Files:** None (testing)

- [ ] **Step 1: Full build of all services**

Run: `cd "C:/Users/ADMIN/Downloads/temp_v12/pcms" && ./apache-maven-3.9.9/bin/mvn package -DskipTests 2>&1 | tail -5`
Expected: `BUILD SUCCESS`

- [ ] **Step 2: Restart order-service and inventory-service**

Run:
```bash
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
netstat -ano | grep ":8088" | head -1 | awk '{print $5}' | xargs -I {} cmd //c "taskkill /F /PID {}" 2>&1 | head -1
netstat -ano | grep ":8086" | head -1 | awk '{print $5}' | xargs -I {} cmd //c "taskkill /F /PID {}" 2>&1 | head -1
sleep 5
nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" -jar inventory-service/target/inventory-service-1.0.0-SNAPSHOT.jar > logs/inventory-service.log 2>&1 &
echo "Started inventory"
nohup env MYSQL_PORT=3306 "/c/Program Files/Common Files/Oracle/Java/javapath/java" -jar order-service/target/order-service-1.0.0-SNAPSHOT.jar > logs/order-service.log 2>&1 &
echo "Started order"
sleep 45
netstat -an | grep -E "8086|8088" | grep LISTENING
```
Expected: Both ports listening

- [ ] **Step 3: Test saga creation**

Get admin token and test the saga admin endpoint:
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d '{"email":"admin@pcms.vn","password":"admin123"}' | grep -oP '"accessToken":"[^"]+' | cut -d'"' -f4)
# Find any orderId and trigger markAsPaid to start a saga
ORDER_ID=$(curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/orders | grep -oP '"id":"[^"]+' | head -1 | cut -d'"' -f4)
echo "Test order: $ORDER_ID"
```

- [ ] **Step 4: Query saga by aggregate**

Run:
```bash
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/admin/saga/by-aggregate/Order/$ORDER_ID | head -c 500
```
Expected: Shows saga details with steps

---

## Task 19: Commit and document

**Files:** None (git operations + docs)

- [ ] **Step 1: Commit saga implementation**

```bash
cd "C:/Users/ADMIN/Downloads/temp_v12/pcms"
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add order-service/src/main/java/com/pcms/orderservice/saga/
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add order-service/src/main/java/com/pcms/orderservice/entity/SagaInstance.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add order-service/src/main/java/com/pcms/orderservice/entity/SagaStep.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add order-service/src/main/java/com/pcms/orderservice/repository/SagaInstanceRepository.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add order-service/src/main/java/com/pcms/orderservice/repository/SagaStepRepository.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add order-service/src/main/java/com/pcms/orderservice/scheduler/SagaTimeoutScheduler.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add order-service/src/main/java/com/pcms/orderservice/service/impl/OrderServiceImpl.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add order-service/src/main/java/com/pcms/orderservice/scheduler/OutboxPublisher.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add inventory-service/src/main/java/com/pcms/inventoryservice/dto/BulkConsumeRequest.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add inventory-service/src/main/java/com/pcms/inventoryservice/dto/BulkRestoreRequest.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add inventory-service/src/main/java/com/pcms/inventoryservice/controller/OutboxConsumerController.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add inventory-service/src/main/java/com/pcms/inventoryservice/service/impl/InventoryServiceImpl.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add inventory-service/src/main/java/com/pcms/inventoryservice/service/impl/OutboxConsumerServiceImpl.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" add payment-service/src/main/java/com/pcms/paymentservice/service/impl/PaymentServiceImpl.java
git -c user.email="huybeoku@gmail.com" -c user.name="h004888" commit -m "feat(saga): implement Orchestration Saga pattern

- Add SagaInstance/SagaStep entities with state machine
- Add SagaOrchestrator for centralized step coordination
- Add SagaCompensationHandler for automatic compensation
- Add SagaTimeoutScheduler to detect stuck sagas
- Add bulk consume/restore endpoints to inventory-service
- Wire OrderServiceImpl.markAsPaid to start saga
- Wire OrderServiceImpl.cancel to trigger compensation
- Add /admin/saga endpoints for inspection"
```

---

## Summary of Changes

| # | Component | Type | Lines (approx) |
|---|-----------|------|----------------|
| 1 | SagaStatus, SagaStepStatus, SagaType enums | New | 30 |
| 2 | SagaInstance entity | New | 80 |
| 3 | SagaStep entity | New | 120 |
| 4 | 2 Saga repositories | New | 30 |
| 5 | BulkConsumeRequest, BulkRestoreRequest | New | 20 |
| 6 | InventoryServiceImpl.bulkConsumeStock + restoreStockByOrder | Modify | 60 |
| 7 | OutboxConsumerController bulk endpoints | Modify | 30 |
| 8 | OutboxConsumerServiceImpl bulk handlers | Modify | 50 |
| 9 | SagaOrchestrator | New | 180 |
| 10 | SagaCompensationHandler | New | 80 |
| 11 | SagaTimeoutScheduler | New | 70 |
| 12 | SagaStepExecutor | New | 80 |
| 13 | OrderServiceImpl.markAsPaid | Modify | 20 |
| 14 | OrderServiceImpl.cancel | Modify | 30 |
| 15 | SagaAdminController | New | 70 |
| 16 | PaymentServiceImpl | Modify | 15 |
| 17 | OutboxPublisher | Modify | 10 |
| | **TOTAL** | | **~985 lines** |

## Expected Outcome

After all 19 tasks completed:
- ✅ Central SagaOrchestrator replaces per-step outbox emission
- ✅ All saga steps tracked in `saga_instances` + `saga_steps` tables
- ✅ Automatic compensation on step failure (reverse order)
- ✅ Compensation for points AND notification (was only inventory before)
- ✅ Stuck-saga detection via SagaTimeoutScheduler (every 5 min, 30 min threshold)
- ✅ Precise stock restoration via `restoreStockByOrder` using InventoryTransaction.refId
- ✅ Bulk stock operations atomic per order
- ✅ Admin API to inspect/manage sagas (`/admin/saga/*`)
- ✅ Backward compatibility: old orders without saga still work