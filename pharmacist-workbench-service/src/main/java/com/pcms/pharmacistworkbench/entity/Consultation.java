package com.pcms.pharmacistworkbench.entity;

import com.pcms.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consultations", indexes = {
        @Index(name = "idx_consult_customer", columnList = "customer_id"),
        @Index(name = "idx_consult_pharmacist", columnList = "pharmacist_id"),
        @Index(name = "idx_consult_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class Consultation extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "pharmacist_id", nullable = false)
    private UUID pharmacistId;

    @Column(nullable = false, length = 20)
    private String channel = "TEXT";

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String transcript;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getPharmacistId() { return pharmacistId; }
    public void setPharmacistId(UUID pharmacistId) { this.pharmacistId = pharmacistId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }
}
