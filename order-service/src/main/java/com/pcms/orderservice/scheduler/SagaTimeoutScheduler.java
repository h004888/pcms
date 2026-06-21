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
                log.info("[saga] Saga {} has {} in-progress steps, waiting",
                        saga.getId(), inProgressSteps.size());
                continue;
            }
            List<SagaStep> pendingSteps = stepRepo.findBySagaIdAndStatus(saga.getId(),
                    SagaStepStatus.PENDING);
            if (!pendingSteps.isEmpty()) {
                log.warn("[saga] Saga {} has {} pending steps - triggering compensation",
                        saga.getId(), pendingSteps.size());
                compensationHandler.compensate(saga.getId(),
                        "Saga timeout: pending steps not executed in " + SAGA_TIMEOUT_MINUTES + " min");
            }
        }
    }
}