package com.pcms.healthtools.repository;

import com.pcms.healthtools.entity.HealthQuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealthQuizResultRepository extends JpaRepository<HealthQuizResult, UUID> {
    List<HealthQuizResult> findByCustomerIdOrderByCompletedAtDesc(UUID customerId);
    List<HealthQuizResult> findByCustomerIdAndQuizSlugOrderByCompletedAtDesc(UUID customerId, String quizSlug);
}
