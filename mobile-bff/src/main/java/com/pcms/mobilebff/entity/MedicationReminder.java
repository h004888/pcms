package com.pcms.mobilebff.entity;

import com.pcms.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medication_reminders", indexes = {
        @Index(name = "idx_reminder_customer", columnList = "customer_id"),
        @Index(name = "idx_reminder_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
public class MedicationReminder extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "family_member_id")
    private UUID familyMemberId;

    @Column(name = "medicine_name", nullable = false, length = 200)
    private String medicineName;

    @Column(name = "dosage", length = 100)
    private String dosage; // "1 viên x 3 lần/ngày"

    @Column(name = "frequency", length = 50)
    private String frequency; // MORNING, NOON, EVENING, NIGHT, CUSTOM

    @Column(name = "schedule_time", length = 20)
    private String scheduleTime; // "08:00,12:00,20:00"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "notes", length = 500)
    private String notes;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getFamilyMemberId() { return familyMemberId; }
    public void setFamilyMemberId(UUID familyMemberId) { this.familyMemberId = familyMemberId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getScheduleTime() { return scheduleTime; }
    public void setScheduleTime(String scheduleTime) { this.scheduleTime = scheduleTime; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
