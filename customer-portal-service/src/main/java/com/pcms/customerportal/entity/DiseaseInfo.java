package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UC14 - SHOP-DISEASE-INFO.
 * Disease information grouped by audience and season.
 */
@Entity
@Table(name = "disease_info", indexes = {
        @Index(name = "idx_disease_slug", columnList = "slug", unique = true),
        @Index(name = "idx_disease_audience", columnList = "target_audience"),
        @Index(name = "idx_disease_season", columnList = "season")
})
@EntityListeners(AuditingEntityListener.class)
public class DiseaseInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 200, unique = true)
    private String slug;

    @Column(name = "target_audience", length = 20)
    private String targetAudience; // NAM, NU, NGUOI_GIA, TRE_EM, ALL

    @Column(length = 10)
    private String season; // XUAN, HA, THU, DONG, ALL

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(length = 10)
    private String severity; // LOW, MEDIUM, HIGH

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTargetAudience() { return targetAudience; }
    public void setTargetAudience(String targetAudience) { this.targetAudience = targetAudience; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}