package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UC14 - VACCINE-LEDGER. Permanent record of administered vaccine doses.
 *
 * <p>Created by branch staff (or pharmacist) AFTER the injection happens.
 * Distinct from {@link VaccineBooking} which is the appointment/reservation.
 *
 * <p>One ledger row per dose (multi-dose vaccines produce multiple rows for
 * the same customer + vaccine).
 */
@Entity
@Table(name = "vaccination_ledger", indexes = {
        @Index(name = "idx_ledger_customer", columnList = "customer_id"),
        @Index(name = "idx_ledger_family_member", columnList = "family_member_id"),
        @Index(name = "idx_ledger_vaccine", columnList = "vaccine_id"),
        @Index(name = "idx_ledger_administered_at", columnList = "administered_at")
})
@EntityListeners(AuditingEntityListener.class)
public class VaccinationLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "family_member_id")
    private UUID familyMemberId;

    @Column(name = "vaccine_id", nullable = false)
    private UUID vaccineId;

    /** Dose number (1-based; e.g. 1 of 2, 2 of 2). */
    @Column(name = "dose_number", nullable = false)
    private Integer doseNumber = 1;

    @Column(name = "administered_at", nullable = false)
    private LocalDateTime administeredAt;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "administered_by")
    private UUID administeredBy; // staff user id

    @Column(length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getFamilyMemberId() { return familyMemberId; }
    public void setFamilyMemberId(UUID familyMemberId) { this.familyMemberId = familyMemberId; }
    public UUID getVaccineId() { return vaccineId; }
    public void setVaccineId(UUID vaccineId) { this.vaccineId = vaccineId; }
    public Integer getDoseNumber() { return doseNumber; }
    public void setDoseNumber(Integer doseNumber) { this.doseNumber = doseNumber; }
    public LocalDateTime getAdministeredAt() { return administeredAt; }
    public void setAdministeredAt(LocalDateTime administeredAt) { this.administeredAt = administeredAt; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }
    public UUID getAdministeredBy() { return administeredBy; }
    public void setAdministeredBy(UUID administeredBy) { this.administeredBy = administeredBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}