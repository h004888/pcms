package com.pcms.reportservice.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "report_schedules")
@EntityListeners(AuditingEntityListener.class)
public class ReportSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "report_type", nullable = false, length = 30)
    private String reportType; // revenue, inventory, staff

    @Column(nullable = false, length = 20)
    private String frequency; // DAILY, WEEKLY, MONTHLY

    @Column(nullable = false, length = 10)
    private String format; // excel, pdf

    @Column(name = "schedule_time")
    private LocalTime scheduleTime;

    @Column(length = 500)
    private String recipients; // comma-separated emails

    @Column(name = "branch_id")
    private UUID branchId;

    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ReportSchedule() {}

    // getters/setters for all fields
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public LocalTime getScheduleTime() { return scheduleTime; }
    public void setScheduleTime(LocalTime scheduleTime) { this.scheduleTime = scheduleTime; }
    public String getRecipients() { return recipients; }
    public void setRecipients(String recipients) { this.recipients = recipients; }
    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
