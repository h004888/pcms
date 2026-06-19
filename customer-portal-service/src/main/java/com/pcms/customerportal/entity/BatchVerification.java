package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UC14 - SHOP-VERIFY-ORIGIN.
 * Stores verified batch information for QR code lookups.
 * Pre-populated by admin/integration; queried by B2C customers.
 */
@Entity
@Table(name = "batch_verification", indexes = {
        @Index(name = "idx_batch_verification_no", columnList = "batch_no", unique = true),
        @Index(name = "idx_batch_verification_medicine", columnList = "medicine_id")
})
@EntityListeners(AuditingEntityListener.class)
public class BatchVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "batch_no", nullable = false, length = 50, unique = true)
    private String batchNo;

    @Column(name = "medicine_id")
    private UUID medicineId;

    @Column(length = 100)
    private String manufacturer;

    @Column(name = "manufactured_at")
    private LocalDate manufacturedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(nullable = false, length = 20)
    private String status = "VERIFIED"; // VERIFIED, COUNTERFEIT, EXPIRED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = "VERIFIED";
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public LocalDate getManufacturedAt() { return manufacturedAt; }
    public void setManufacturedAt(LocalDate manufacturedAt) { this.manufacturedAt = manufacturedAt; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}