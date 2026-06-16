package com.pcms.reportservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record StaffReportRequest(
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        UUID branchId
) {
}
