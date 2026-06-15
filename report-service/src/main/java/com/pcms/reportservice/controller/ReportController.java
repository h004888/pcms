package com.pcms.reportservice.controller;

import com.pcms.reportservice.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * UC09 - View Reports (SCR-REPORT)
 * UC10 - Search/filter inputs
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * GET /api/v1/reports/revenue - Step 1-5 of UC09 main flow
     * Query params: from, to (date), branchId, groupBy (day|week|month)
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> revenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(reportService.generateRevenueReport(from, to, branchId, groupBy));
    }

    /**
     * GET /api/v1/reports/inventory - Inventory snapshot
     */
    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> inventory(@RequestParam(required = false) UUID branchId) {
        return ResponseEntity.ok(reportService.generateInventoryReport(branchId));
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
        return ResponseEntity.status(501).body(Map.of(
            "code", "MSG26",
            "message", "Export to be implemented (FR9.3/FR9.4) - use Apache POI for Excel, iText for PDF"
        ));
    }
}
