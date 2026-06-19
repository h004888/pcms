package com.pcms.healthtools.entity;

import com.pcms.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "health_quizzes", indexes = {
        @Index(name = "idx_quiz_slug", columnList = "slug", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class HealthQuiz extends BaseEntity {

    @Column(nullable = false, length = 100, unique = true)
    private String slug; // memory, pre-diabetes, thyroid, asthma, cardiac, alzheimer, gerd, inhaler

    @Column(nullable = false, length = 200)
    private String name;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(name = "questions_json", columnDefinition = "TEXT")
    private String questionsJson; // JSON array of questions

    @Column(length = 50)
    private String scoringLogic; // E.g., "sum >= 10 -> HIGH"

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getQuestionsJson() { return questionsJson; }
    public void setQuestionsJson(String questionsJson) { this.questionsJson = questionsJson; }
    public String getScoringLogic() { return scoringLogic; }
    public void setScoringLogic(String scoringLogic) { this.scoringLogic = scoringLogic; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
