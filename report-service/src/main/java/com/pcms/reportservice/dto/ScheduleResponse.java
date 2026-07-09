package com.pcms.reportservice.dto;

import com.pcms.reportservice.entity.ReportSchedule;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleResponse(
    UUID id,
    String reportType,
    String frequency,
    String format,
    LocalTime scheduleTime,
    String recipients,
    UUID branchId,
    boolean active,
    LocalDateTime createdAt
) {
    public static ScheduleResponse from(ReportSchedule s) {
        return new ScheduleResponse(s.getId(), s.getReportType(), s.getFrequency(),
            s.getFormat(), s.getScheduleTime(), s.getRecipients(),
            s.getBranchId(), s.isActive(), s.getCreatedAt());
    }
}
