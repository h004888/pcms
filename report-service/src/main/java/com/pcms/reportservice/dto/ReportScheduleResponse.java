package com.pcms.reportservice.dto;

import com.pcms.reportservice.entity.ReportSchedule;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportScheduleResponse(
        UUID id,
        String type,
        String format,
        UUID branchId,
        String cronExpression,
        String recipientEmail,
        UUID createdBy,
        boolean active,
        LocalDateTime lastRunAt,
        LocalDateTime nextRunAt,
        String lastStatus,
        String lastMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static ReportScheduleResponse from(ReportSchedule schedule) {
        return new ReportScheduleResponse(
                schedule.getId(),
                schedule.getType(),
                schedule.getFormat(),
                schedule.getBranchId(),
                schedule.getCronExpression(),
                schedule.getRecipientEmail(),
                schedule.getCreatedBy(),
                schedule.isActive(),
                schedule.getLastRunAt(),
                schedule.getNextRunAt(),
                schedule.getLastStatus(),
                schedule.getLastMessage(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt());
    }
}