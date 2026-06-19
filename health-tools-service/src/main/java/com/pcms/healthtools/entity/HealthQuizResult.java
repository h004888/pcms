package com.pcms.healthtools.entity;

import com.pcms.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "health_quiz_results", indexes = {
        @Index(name = "idx_quiz_result_customer", columnList = "customer_id")
})
@EntityListeners(AuditingEntityListener.class)
public class HealthQuizResult extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "quiz_slug", nullable = false, length = 100)
    private String quizSlug;

    @Lob
    @Column(name = "answers_json", columnDefinition = "TEXT")
    private String answersJson;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel; // LOW, MODERATE, HIGH, etc.

    @Lob
    @Column(name = "advice", columnDefinition = "TEXT")
    private String advice;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getQuizSlug() { return quizSlug; }
    public void setQuizSlug(String quizSlug) { this.quizSlug = quizSlug; }
    public String getAnswersJson() { return answersJson; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
