package com.pcms.inventoryservice.entity;

import com.pcms.inventoryservice.enums.TransactionType;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UC05 - SRS §3.1.6 Entity 9
 * Stock movement audit trail (CR-09)
 */
@Entity
@Table(name = "inventory_transactions")
@EntityListeners(AuditingEntityListener.class)
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    /** Delta units (positive for IMPORT/TRANSFER_IN, negative for EXPORT/TRANSFER_OUT/SALE) */
    @Column(nullable = false)
    private Integer qty;

    @Column(length = 255)
    private String reason;

    /** Reference to order/PO */
    @Column(name = "ref_id")
    private UUID refId;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public InventoryTransaction() {}

    public InventoryTransaction(UUID batchId, TransactionType type, Integer qty, UUID actorId) {
        this.batchId = batchId;
        this.type = type;
        this.qty = qty;
        this.actorId = actorId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public UUID getRefId() { return refId; }
    public void setRefId(UUID refId) { this.refId = refId; }
    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
