package com.pcms.mobilebff.entity;

import com.pcms.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medication_intakes", indexes = {
        @Index(name = "idx_intake_reminder", columnList = "reminder_id")
})
@EntityListeners(AuditingEntityListener.class)
public class MedicationIntake extends BaseEntity {

    @Column(name = "reminder_id", nullable = false)
    private UUID reminderId;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, TAKEN, SKIPPED, LATE

    @Column(length = 200)
    private String note;

    public UUID getReminderId() { return reminderId; }
    public void setReminderId(UUID reminderId) { this.reminderId = reminderId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime takenAt) { this.takenAt = takenAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
