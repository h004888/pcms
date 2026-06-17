package com.pcms.inventoryservice.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UC05 - SRS §3.1.6 Entity 8
 * Per-branch batch stock; FIFO by expiry_date (NSF-05)
 */
@Entity
@Table(name = "inventory_batches", uniqueConstraints = {
        @UniqueConstraint(name = "uk_batch_medicine_branch_no", columnNames = { "medicine_id", "branch_id",
                "batch_no" }),
        @UniqueConstraint(name = "uk_batch_barcode", columnNames = { "barcode" })
})
@EntityListeners(AuditingEntityListener.class)
public class InventoryBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "medicine_id", nullable = false)
    private UUID medicineId;

    @Column(name = "branch_id", nullable = false)
    private UUID branchId;

    @Column(name = "batch_no", nullable = false, length = 30)
    private String batchNo;

    @Column(name = "barcode", nullable = false, length = 60)
    private String barcode;

    @Column(name = "qty_on_hand", nullable = false)
    private Integer qtyOnHand;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "min_stock_level", nullable = false)
    private Integer minStockLevel = 10;

    @CreatedDate
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    public InventoryBatch() {
    }

    public InventoryBatch(UUID medicineId, UUID branchId, String batchNo, Integer qtyOnHand, LocalDate expiryDate) {
        this.medicineId = medicineId;
        this.branchId = branchId;
        this.batchNo = batchNo;
        this.barcode = batchNo;
        this.qtyOnHand = qtyOnHand;
        this.expiryDate = expiryDate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(UUID medicineId) {
        this.medicineId = medicineId;
    }

    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Integer getQtyOnHand() {
        return qtyOnHand;
    }

    public void setQtyOnHand(Integer qtyOnHand) {
        this.qtyOnHand = qtyOnHand;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Integer getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(Integer minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
