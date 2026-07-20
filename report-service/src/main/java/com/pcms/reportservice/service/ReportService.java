package com.pcms.reportservice.service;

import com.pcms.reportservice.dto.InventoryReportRequest;
import com.pcms.reportservice.dto.InventoryReportResponse;
import com.pcms.reportservice.dto.RevenueReportRequest;
import com.pcms.reportservice.dto.RevenueReportResponse;
import com.pcms.reportservice.dto.StaffReportRequest;
import com.pcms.reportservice.dto.StaffReportResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC09 - Reports
 * <p>
 * Originally a single class, refactored to an interface with the
 * implementation in
 * {@link com.pcms.reportservice.service.impl.ReportServiceImpl}.
 * <p>
 * FR9.1, FR9.2 - Revenue, Inventory reports
 * FR9.3, FR9.4 - Excel/PDF export
 * FR9.5 - Schedule delivery (TBD)
 */
public interface ReportService {

    /**
     * Aggregate orders by day/week/month in a date range.
     */
    RevenueReportResponse revenue(RevenueReportRequest request);

    /**
     * Backwards-compatible signature used by the existing controller.
     */
    RevenueReportResponse revenue(LocalDate from, LocalDate to, UUID branchId, String groupBy);

    /**
     * Snapshot of current stock per branch with low-stock alerts.
     */
    InventoryReportResponse inventory(InventoryReportRequest request);

    /**
     * Backwards-compatible signature used by the existing controller.
     */
    InventoryReportResponse inventory(UUID branchId);

    /**
     * Group orders/payments by staff.
     */
    StaffReportResponse staff(StaffReportRequest request);

    Map<String, Object> realtimeStats(UUID branchId);

    List<Map<String, Object>> recentOrders(UUID branchId, int limit);

    /**
     * Generate Excel or PDF exports of a report.
     */
    Object export(String type, String format, LocalDate from, LocalDate to);

}
