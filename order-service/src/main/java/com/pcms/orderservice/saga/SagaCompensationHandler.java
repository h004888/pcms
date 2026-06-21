package com.pcms.orderservice.saga;

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
import java.util.List;
import java.util.UUID;

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
    private final OutboxEventRepository outboxRepo;

    public SagaCompensationHandler(SagaInstanceRepository sagaRepo, SagaStepRepository stepRepo,
            OutboxEventRepository outboxRepo) {
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
        if (saga.getStatus() != SagaStatus.IN_PROGRESS && saga.getStatus() != SagaStatus.COMPENSATING
                && saga.getStatus() != SagaStatus.STARTED) {
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
                OutboxEvent event = new OutboxEvent(
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