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

    java.util.Optional<SagaStep> findByOutboxEventId(UUID outboxEventId);
}