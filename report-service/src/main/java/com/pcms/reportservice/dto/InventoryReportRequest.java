package com.pcms.reportservice.dto;

import java.time.LocalDate;
import java.util.UUID;

public record InventoryReportRequest(
        UUID branchId,
        LocalDate fromDate,
        LocalDate toDate
) {
}
