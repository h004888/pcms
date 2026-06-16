package com.pcms.reportservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record RevenueReportRequest(
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        UUID branchId,
        GroupBy groupBy
) {
    public enum GroupBy { DAY, WEEK, MONTH }
}
