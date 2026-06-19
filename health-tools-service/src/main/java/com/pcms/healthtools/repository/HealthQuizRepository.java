package com.pcms.healthtools.repository;

import com.pcms.healthtools.entity.HealthQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HealthQuizRepository extends JpaRepository<HealthQuiz, UUID> {
    Optional<HealthQuiz> findBySlug(String slug);
    List<HealthQuiz> findByStatusOrderByNameAsc(String status);
}
