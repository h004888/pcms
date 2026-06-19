package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

/**
 * UC14 / UC19 - Product review & rating.
 * Per FR19.5: 1-5 stars + text + images. Per FR19.6: auto-AI + manual moderation.
 * Per Sprint 4 plan: only schema + repository. Moderation flow lands in Sprint 10.
 */
@Entity
@Table(name = "product_reviews", indexes = {
        @Index(name = "idx_review_medicine", columnList = "medicine_id"),
        @Index(name = "idx_review_customer", columnList = "customer_id"),
        @Index(name = "idx_review_status", columnList = "status"),
        @Index(name = "idx_review_rating", columnList = "rating")
})
@EntityListeners(AuditingEntityListener.class)
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "medicine_id", nullable = false)
    private UUID medicineId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private Integer rating; // 1-5

    @Column(length = 1000)
    private String body;

    /** JSON array of image URLs */
    @Column(name = "image_urls", length = 2000)
    private String imageUrls;

    /** PENDING / APPROVED / REJECTED (per FR19.6) */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = false;

    @Column(name = "helpful_count", nullable = false)
    private Long helpfulCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }
    public Long getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(Long helpfulCount) { this.helpfulCount = helpfulCount; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
