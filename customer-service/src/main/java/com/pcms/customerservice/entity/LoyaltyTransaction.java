package com.pcms.customerservice.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Loyalty points transaction log (BR07 / NSF-04).
 *
 * <p>Each call to {@code addPoints} creates a row here for audit + idempotency.
 * Idempotency: a {@code ref_order_id} is UNIQUE — if the same order triggers
 * addPoints twice (e.g., webhook retry), the second call is a no-op and the
 * customer is NOT double-credited.
 */
@Entity
@Table(
    name = "loyalty_transactions",
    uniqueConstraints = @UniqueConstraint(name = "uk_loyalty_ref_order", columnNames = "ref_order_id")
)
@EntityListeners(AuditingEntityListener.class)
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private Integer points;       // can be negative for refund/revoke

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter; // customer's points after this tx

    @Column(name = "ref_order_id")
    private UUID refOrderId;      // unique → idempotency key (nullable for non-order txs)

    @Column(length = 255)
    private String reason;        // e.g. "ORDER_PAID:ORD-20260616-0001"

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public LoyaltyTransaction() {}

    public LoyaltyTransaction(UUID customerId, Integer points, Integer balanceAfter,
                              UUID refOrderId, String reason) {
        this.customerId = customerId;
        this.points = points;
        this.balanceAfter = balanceAfter;
        this.refOrderId = refOrderId;
        this.reason = reason;
    }

    // Getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }
    public UUID getRefOrderId() { return refOrderId; }
    public void setRefOrderId(UUID refOrderId) { this.refOrderId = refOrderId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
