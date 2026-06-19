package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

/**
 * UC14 - SHOP-VIDEO. Short-form medical videos (Bộ Y tế, WHO).
 * Stored as YouTube ID rather than self-hosted to keep hosting simple.
 */
@Entity
@Table(name = "videos", indexes = {
        @Index(name = "idx_video_category", columnList = "category"),
        @Index(name = "idx_video_youtube", columnList = "youtube_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    /** YouTube video ID (11 chars) - embedded via standard YouTube iframe */
    @Column(name = "youtube_id", nullable = false, length = 32)
    private String youtubeId;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(length = 100)
    private String source; // "BoYTe", "WHO", "PCMS", etc.

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(length = 50)
    private String category;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getYoutubeId() { return youtubeId; }
    public void setYoutubeId(String youtubeId) { this.youtubeId = youtubeId; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
