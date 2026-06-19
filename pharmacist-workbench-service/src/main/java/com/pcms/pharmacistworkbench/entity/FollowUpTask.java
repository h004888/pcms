package com.pcms.pharmacistworkbench.entity;

import com.pcms.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "follow_up_tasks", indexes = {
        @Index(name = "idx_fu_customer", columnList = "customer_id"),
        @Index(name = "idx_fu_scheduled", columnList = "scheduled_at"),
        @Index(name = "idx_fu_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class FollowUpTask extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "prescription_id")
    private UUID prescriptionId;

    @Column(nullable = false, length = 20)
    private String type = "FEEDBACK";

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(length = 20)
    private String response;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String note;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(UUID prescriptionId) { this.prescriptionId = prescriptionId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
