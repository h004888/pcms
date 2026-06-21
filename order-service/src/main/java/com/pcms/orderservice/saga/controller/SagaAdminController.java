package com.pcms.orderservice.saga.controller;

import com.pcms.orderservice.entity.SagaInstance;
import com.pcms.orderservice.entity.SagaStep;
import com.pcms.orderservice.repository.SagaInstanceRepository;
import com.pcms.orderservice.repository.SagaStepRepository;
import com.pcms.orderservice.saga.SagaCompensationHandler;
import com.pcms.orderservice.saga.SagaStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
                        LocalDateTime.now().minusMinutes(30))));
    }
}