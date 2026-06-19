package com.pcms.customerportal.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UC14 CUST-NOTIF-SETTINGS - Cài đặt thông báo (FR14.26).
 * <p>One row per customer (UNIQUE on customer_id). Missing row means
 * "use defaults" - the service lazily creates a default row on first read.
 */
@Entity
@Table(name = "customer_notif_settings",
       uniqueConstraints = @UniqueConstraint(name = "uk_notifset_customer",
                                             columnNames = "customer_id"))
@EntityListeners(AuditingEntityListener.class)
public class CustomerNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private UUID customerId;

    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    private Boolean smsEnabled = true;

    @Column(name = "marketing_enabled", nullable = false)
    private Boolean marketingEnabled = false;

    @Column(name = "order_updates", nullable = false)
    private Boolean orderUpdates = true;

    @Column(name = "low_stock_alert", nullable = false)
    private Boolean lowStockAlert = true;

    @Column(name = "expiry_alert", nullable = false)
    private Boolean expiryAlert = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public Boolean getPushEnabled() { return pushEnabled; }
    public void setPushEnabled(Boolean pushEnabled) { this.pushEnabled = pushEnabled; }
    public Boolean getEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(Boolean emailEnabled) { this.emailEnabled = emailEnabled; }
    public Boolean getSmsEnabled() { return smsEnabled; }
    public void setSmsEnabled(Boolean smsEnabled) { this.smsEnabled = smsEnabled; }
    public Boolean getMarketingEnabled() { return marketingEnabled; }
    public void setMarketingEnabled(Boolean marketingEnabled) { this.marketingEnabled = marketingEnabled; }
    public Boolean getOrderUpdates() { return orderUpdates; }
    public void setOrderUpdates(Boolean orderUpdates) { this.orderUpdates = orderUpdates; }
    public Boolean getLowStockAlert() { return lowStockAlert; }
    public void setLowStockAlert(Boolean lowStockAlert) { this.lowStockAlert = lowStockAlert; }
    public Boolean getExpiryAlert() { return expiryAlert; }
    public void setExpiryAlert(Boolean expiryAlert) { this.expiryAlert = expiryAlert; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
