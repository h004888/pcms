package com.pcms.pharmacistworkbench.entity;

import com.pcms.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vip_marks", indexes = {
        @Index(name = "idx_vip_customer", columnList = "customer_id", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class VipMark extends BaseEntity {

    @Column(name = "customer_id", nullable = false, unique = true)
    private UUID customerId;

    @Column(name = "marked_by", nullable = false)
    private UUID markedBy;

    @Column(name = "tier", length = 20)
    private String tier = "GOLD";

    @Column(name = "loyalty_score")
    private Integer loyaltyScore = 0;

    @Column(name = "lifetime_spend", precision = 15, scale = 2)
    private BigDecimal lifetimeSpend = BigDecimal.ZERO;

    @Column(name = "marked_at", nullable = false)
    private LocalDateTime markedAt = LocalDateTime.now();

    @Lob
    @Column(columnDefinition = "TEXT")
    private String reason;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getMarkedBy() { return markedBy; }
    public void setMarkedBy(UUID markedBy) { this.markedBy = markedBy; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public Integer getLoyaltyScore() { return loyaltyScore; }
    public void setLoyaltyScore(Integer loyaltyScore) { this.loyaltyScore = loyaltyScore; }
    public BigDecimal getLifetimeSpend() { return lifetimeSpend; }
    public void setLifetimeSpend(BigDecimal lifetimeSpend) { this.lifetimeSpend = lifetimeSpend; }
    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
