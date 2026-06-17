package com.pcms.reportservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReportScheduleRequest(
        @NotBlank(message = "Loại báo cáo không được để trống") @Size(max = 30, message = "Loại báo cáo không được vượt quá 30 ký tự") String type,

        @NotBlank(message = "Định dạng không được để trống") @Size(max = 10, message = "Định dạng không được vượt quá 10 ký tự") String format,

        UUID branchId,

        @NotBlank(message = "Biểu thức cron không được để trống") @Size(max = 100, message = "Biểu thức cron không được vượt quá 100 ký tự") String cronExpression,

        @NotBlank(message = "Email nhận không được để trống") @Size(max = 100, message = "Email nhận không được vượt quá 100 ký tự") String recipientEmail,

        @NotNull(message = "Người tạo lịch không được để trống") UUID createdBy) {
}