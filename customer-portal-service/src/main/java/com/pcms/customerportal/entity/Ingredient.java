package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

/**
 * UC14 - SHOP-LOOKUP-INGREDIENT. Active pharmaceutical ingredient
 * (e.g. Paracetamol, Amoxicillin). Used for B2C drug lookup by hoạt chất.
 */
@Entity
@Table(name = "ingredients", indexes = {
        @Index(name = "idx_ingredient_name_vi", columnList = "name_vi"),
        @Index(name = "idx_ingredient_name_en", columnList = "name_en")
})
@EntityListeners(AuditingEntityListener.class)
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name_vi", nullable = false, length = 200)
    private String nameVi;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    /** Comma-separated synonyms (e.g. "Acetaminophen,Tylenol,APAP") */
    @Column(length = 1000)
    private String synonyms;

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
    public String getSynonyms() { return synonyms; }
    public void setSynonyms(String synonyms) { this.synonyms = synonyms; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}