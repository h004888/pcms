package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UC14 CUST-FAVORITES - Sản phẩm yêu thích (FR14.25).
 * <p>One row per (customer, medicine) pair; UNIQUE constraint prevents
 * duplicates. add() is therefore idempotent.
 */
@Entity
@Table(name = "customer_favorites",
       uniqueConstraints = @UniqueConstraint(name = "uk_fav_customer_medicine",
                                             columnNames = {"customer_id", "medicine_id"}),
       indexes = {
           @Index(name = "idx_fav_customer", columnList = "customer_id"),
           @Index(name = "idx_fav_medicine", columnList = "medicine_id")
       })
@EntityListeners(AuditingEntityListener.class)
public class CustomerFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "medicine_id", nullable = false)
    private UUID medicineId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
