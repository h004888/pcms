package com.pcms.reportservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalTime;
import java.util.UUID;

public record CreateScheduleRequest(
    @NotBlank String reportType,
    @NotBlank String frequency,
    @NotBlank String format,
    LocalTime scheduleTime,
    String recipients,
    UUID branchId
) {}
