package com.pcms.customerportal.entity;

import com.pcms.customerportal.enums.WalletTransactionType;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UC14 CUST-HEALTH-WALLET - Health wallet transaction ledger (FR14.23).
 * <p>Each row is an immutable record of points earned/redeemed/expired/adjusted.
 * The running balance is computed via {@code SUM(amount)} over the customer;
 * we also store {@code balanceAfter} for audit/debugging convenience.
 */
@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_wallet_tx_customer", columnList = "customer_id"),
        @Index(name = "idx_wallet_tx_type", columnList = "type"),
        @Index(name = "idx_wallet_tx_created", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletTransactionType type;

    /** Positive for EARN/ADJUST+, negative for REDEEM/EXPIRE. */
    @Column(nullable = false)
    private Integer amount;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    /** Free-text source: ORDER_PAID, REDEMPTION, ADMIN, EXPIRY, etc. */
    @Column(nullable = false, length = 100)
    private String source;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(columnDefinition = "TEXT")
    private String note;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public WalletTransactionType getType() { return type; }
    public void setType(WalletTransactionType type) { this.type = type; }
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public UUID getSourceId() { return sourceId; }
    public void setSourceId(UUID sourceId) { this.sourceId = sourceId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
