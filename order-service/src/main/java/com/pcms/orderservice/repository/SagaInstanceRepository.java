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