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
import java.util.UUID;

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
    public void onStepSuccess(UUID outboxEventId) {
        SagaStep step = stepRepo.findByOutboxEventId(outboxEventId).orElse(null);
        if (step == null) return;
        step.setStatus(SagaStepStatus.COMPLETED);
        step.setCompletedAt(LocalDateTime.now());
        stepRepo.save(step);
        log.info("[saga] Step {} ({}) completed", step.getId(), step.getStepName());
        checkSagaCompletion(step.getSaga().getId());
    }

    @Transactional
    public void onStepFailure(UUID outboxEventId, String errorMessage) {
        SagaStep step = stepRepo.findByOutboxEventId(outboxEventId).orElse(null);
        if (step == null) return;
        step.setStatus(SagaStepStatus.FAILED);
        step.setErrorMessage(errorMessage);
        step.setRetryCount(step.getRetryCount() + 1);
        stepRepo.save(step);
        log.error("[saga] Step {} ({}) FAILED: {}", step.getId(), step.getStepName(), errorMessage);
        compensationHandler.compensate(step.getSaga().getId(),
                "Step " + step.getStepName() + " failed: " + errorMessage);
    }

    private void checkSagaCompletion(UUID sagaId) {
        SagaInstance saga = sagaRepo.findById(sagaId).orElse(null);
        if (saga == null) return;
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