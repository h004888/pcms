package com.pcms.reportservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * TICKET-305 / TICKET-306: Common request body for {@code POST /reports/export/excel}
 * and {@code POST /reports/export/pdf}. Mirrors the existing
 * {@code GET /reports/export} query parameters so callers can stay
 * format-agnostic.
 *
 * <p>Either {@code from+to} (date range) or {@code branchId} may be supplied;
 * the implementation resolves a default 7-day window when both are absent.
 *
 * @param reportType one of {@code revenue|inventory|staff}
 * @param format     one of {@code excel|pdf} (enforced at controller level for
 *                   the two dedicated endpoints)
 * @param branchId   optional branch filter
 * @param fromDate   optional start of the report window (inclusive)
 * @param toDate     optional end of the report window (inclusive)
 * @param filters    free-form additional filters (groupBy, etc.)
 */
public record ReportExportRequest(
        @NotBlank
        @Pattern(regexp = "revenue|inventory|staff",
                 message = "reportType must be one of: revenue, inventory, staff")
        String reportType,

        String format,

        UUID branchId,

        LocalDate fromDate,

        LocalDate toDate,

        Map<String, Object> filters
) {
}
