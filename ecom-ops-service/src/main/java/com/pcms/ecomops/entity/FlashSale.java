package com.pcms.ecomops.entity;

import com.pcms.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flash_sales", indexes = {
        @Index(name = "idx_flash_status", columnList = "status"),
        @Index(name = "idx_flash_window", columnList = "starts_at, ends_at")
})
@EntityListeners(AuditingEntityListener.class)
public class FlashSale extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Column(name = "discount_pct", precision = 5, scale = 2)
    private BigDecimal discountPct;

    @Column(name = "max_qty_per_user")
    private Integer maxQtyPerUser = 1;

    @Column(nullable = false, length = 20)
    private String status = "SCHEDULED"; // SCHEDULED, ACTIVE, ENDED, CANCELLED

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }
    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }
    public BigDecimal getDiscountPct() { return discountPct; }
    public void setDiscountPct(BigDecimal discountPct) { this.discountPct = discountPct; }
    public Integer getMaxQtyPerUser() { return maxQtyPerUser; }
    public void setMaxQtyPerUser(Integer maxQtyPerUser) { this.maxQtyPerUser = maxQtyPerUser; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
