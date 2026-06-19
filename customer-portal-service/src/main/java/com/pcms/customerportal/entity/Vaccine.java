package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * UC14 - VACCINE-HOME. Vaccine catalog master data.
 *
 * <p>Distinct from {@link com.pcms.customerportal.entity.Medicine} because
 * vaccines have scheduling semantics (doses, intervals) that don't apply
 * to retail medicines.
 *
 * <p>Status: ACTIVE / INACTIVE (soft delete per CR-08).
 */
@Entity
@Table(name = "vaccines", indexes = {
        @Index(name = "idx_vaccine_status", columnList = "status"),
        @Index(name = "idx_vaccine_manufacturer", columnList = "manufacturer")
})
@EntityListeners(AuditingEntityListener.class)
public class Vaccine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String manufacturer;

    /** Number of doses required for full immunisation. */
    @Column(name = "doses_required", nullable = false)
    private Integer dosesRequired = 1;

    /** Days between consecutive doses (for multi-dose vaccines). */
    @Column(name = "days_between_doses")
    private Integer daysBetweenDoses;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** ACTIVE / INACTIVE */
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public Integer getDosesRequired() { return dosesRequired; }
    public void setDosesRequired(Integer dosesRequired) { this.dosesRequired = dosesRequired; }
    public Integer getDaysBetweenDoses() { return daysBetweenDoses; }
    public void setDaysBetweenDoses(Integer daysBetweenDoses) { this.daysBetweenDoses = daysBetweenDoses; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}