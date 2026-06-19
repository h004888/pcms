package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

/**
 * UC14 - SHOP-LOOKUP-HERB. Traditional Vietnamese herb / vị thuốc cổ truyền.
 */
@Entity
@Table(name = "herbs", indexes = {
        @Index(name = "idx_herb_name_vi", columnList = "name_vi"),
        @Index(name = "idx_herb_name_en", columnList = "name_en")
})
@EntityListeners(AuditingEntityListener.class)
public class Herb {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name_vi", nullable = false, length = 200)
    private String nameVi;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(name = "traditional_use", length = 2000)
    private String traditionalUse;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNameVi() { return nameVi; }
    public void setNameVi(String nameVi) { this.nameVi = nameVi; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public String getTraditionalUse() { return traditionalUse; }
    public void setTraditionalUse(String traditionalUse) { this.traditionalUse = traditionalUse; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}