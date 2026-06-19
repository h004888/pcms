package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * UC14 - VACCINE-BOOKING. A bookable injection slot at a branch on a date.
 *
 * <p>Atomic decrement of {@code availableQty} is enforced via
 * {@code PESSIMISTIC_WRITE} lock on the row when a booking is created or
 * cancelled (see {@code VaccineSlotRepository#lockById}).
 */
@Entity
@Table(name = "vaccine_slots", indexes = {
        @Index(name = "idx_slot_vaccine", columnList = "vaccine_id"),
        @Index(name = "idx_slot_branch_date", columnList = "branch_id,slot_date"),
        @Index(name = "idx_slot_date", columnList = "slot_date")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_slot_unique",
                columnNames = {"vaccine_id", "branch_id", "slot_date", "slot_time"})
})
@EntityListeners(AuditingEntityListener.class)
public class VaccineSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "vaccine_id", nullable = false)
    private UUID vaccineId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Column(name = "available_qty", nullable = false)
    private Integer availableQty = 0;

    @Column(name = "total_qty", nullable = false)
    private Integer totalQty = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getVaccineId() { return vaccineId; }
    public void setVaccineId(UUID vaccineId) { this.vaccineId = vaccineId; }
    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }
    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }
    public LocalTime getSlotTime() { return slotTime; }
    public void setSlotTime(LocalTime slotTime) { this.slotTime = slotTime; }
    public Integer getAvailableQty() { return availableQty; }
    public void setAvailableQty(Integer availableQty) { this.availableQty = availableQty; }
    public Integer getTotalQty() { return totalQty; }
    public void setTotalQty(Integer totalQty) { this.totalQty = totalQty; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}