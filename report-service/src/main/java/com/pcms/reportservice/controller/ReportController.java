package com.pcms.reportservice.controller;

import com.pcms.reportservice.dto.InventoryReportRequest;
import com.pcms.reportservice.dto.InventoryReportResponse;
import com.pcms.reportservice.dto.RevenueReportRequest;
import com.pcms.reportservice.dto.RevenueReportResponse;
import com.pcms.reportservice.dto.StaffReportRequest;
import com.pcms.reportservice.dto.StaffReportResponse;
import com.pcms.reportservice.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * UC09 - View Reports (SCR-REPORT)
 * UC10 - Search/filter inputs
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * GET /api/v1/reports/revenue - Step 1-5 of UC09 main flow
     * Query params: from, to (date), branchId, groupBy (day|week|month)
     */
    @GetMapping("/revenue")
    public ResponseEntity<RevenueReportResponse> revenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(reportService.revenue(from, to, branchId, groupBy));
    }

    /** POST variant accepting a JSON request body. */
    @PostMapping("/revenue")
    public ResponseEntity<RevenueReportResponse> revenuePost(@Valid @RequestBody RevenueReportRequest request) {
        return ResponseEntity.ok(reportService.revenue(request));
    }

    /**
     * GET /api/v1/reports/inventory - Inventory snapshot
     */
    @GetMapping("/inventory")
    public ResponseEntity<InventoryReportResponse> inventory(@RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(reportService.inventory(branchId));
    }

    /** POST variant accepting a JSON request body. */
    @PostMapping("/inventory")
    public ResponseEntity<InventoryReportResponse> inventoryPost(@Valid @RequestBody InventoryReportRequest request) {
        return ResponseEntity.ok(reportService.inventory(request));
    }

    /** Staff performance report. */
    @PostMapping("/staff")
    public ResponseEntity<StaffReportResponse> staff(@Valid @RequestBody StaffReportRequest request) {
        return ResponseEntity.ok(reportService.staff(request));
    }

    /**
     * GET /api/v1/reports/export - Step 6-8 of UC09 main flow
     * TODO: implement Excel/PDF generation
     */
    @GetMapping("/export")
    public ResponseEntity<?> export(
            @RequestParam String type,    // revenue | inventory | staff
            @RequestParam String format,  // excel | pdf
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.status(501).body(reportService.export(type, format, from, to));
    }
}
