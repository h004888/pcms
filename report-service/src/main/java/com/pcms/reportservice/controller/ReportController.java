package com.pcms.reportservice.controller;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.reportservice.dto.InventoryReportRequest;
import com.pcms.reportservice.dto.InventoryReportResponse;
import com.pcms.reportservice.dto.CreateReportScheduleRequest;
import com.pcms.reportservice.dto.ReportExportRequest;
import com.pcms.reportservice.dto.ReportScheduleResponse;
import com.pcms.reportservice.dto.RevenueReportRequest;
import com.pcms.reportservice.dto.RevenueReportResponse;
import com.pcms.reportservice.dto.StaffReportRequest;
import com.pcms.reportservice.dto.StaffReportResponse;
import com.pcms.reportservice.service.ExcelExportService;
import com.pcms.reportservice.service.PdfExportService;
import com.pcms.reportservice.service.ReportScheduleService;
import com.pcms.reportservice.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC09 - View Reports (SCR-REPORT)
 * UC10 - Search/filter inputs
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportScheduleService reportScheduleService;
    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    public ReportController(ReportService reportService,
                            ReportScheduleService reportScheduleService,
                            ExcelExportService excelExportService,
                            PdfExportService pdfExportService) {
        this.reportService = reportService;
        this.reportScheduleService = reportScheduleService;
        this.excelExportService = excelExportService;
        this.pdfExportService = pdfExportService;
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
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId,
            @RequestParam(defaultValue = "day") String groupBy) {
        return ResponseEntity.ok(reportService.revenue(from, to, resolveBranchId(branchId, currentBranchId), groupBy));
    }

    /** POST variant accepting a JSON request body. */
    @PostMapping("/revenue")
    public ResponseEntity<RevenueReportResponse> revenuePost(
            @Valid @RequestBody RevenueReportRequest request,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return ResponseEntity.ok(reportService.revenue(new RevenueReportRequest(
                request.fromDate(),
                request.toDate(),
                resolveBranchId(request.branchId(), currentBranchId),
                request.groupBy())));
    }

    /**
     * GET /api/v1/reports/inventory - Inventory snapshot
     */
    @GetMapping("/inventory")
    public ResponseEntity<InventoryReportResponse> inventory(
            @RequestParam(required = false) UUID branchId,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return ResponseEntity.ok(reportService.inventory(resolveBranchId(branchId, currentBranchId)));
    }

    /** POST variant accepting a JSON request body. */
    @PostMapping("/inventory")
    public ResponseEntity<InventoryReportResponse> inventoryPost(
            @Valid @RequestBody InventoryReportRequest request,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return ResponseEntity.ok(reportService.inventory(new InventoryReportRequest(
                resolveBranchId(request.branchId(), currentBranchId),
                request.fromDate(),
                request.toDate())));
    }

    /** Staff performance report (GET with query params). */
    @GetMapping("/staff")
    public ResponseEntity<StaffReportResponse> staffGet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID branchId,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return ResponseEntity.ok(reportService.staff(
                new StaffReportRequest(fromDate, toDate, resolveBranchId(branchId, currentBranchId))));
    }

    /** Staff performance report (POST with JSON body). */
    @PostMapping("/staff")
    public ResponseEntity<StaffReportResponse> staff(
            @Valid @RequestBody StaffReportRequest request,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return ResponseEntity.ok(reportService.staff(new StaffReportRequest(
                request.fromDate(),
                request.toDate(),
                resolveBranchId(request.branchId(), currentBranchId))));
    }

    /** B9: GET /api/v1/reports/realtime/stats — today's summary. */
    @GetMapping("/realtime/stats")
    public ResponseEntity<Map<String, Object>> realtimeStats(
            @RequestParam(required = false) UUID branchId,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return ResponseEntity.ok(reportService.realtimeStats(resolveBranchId(branchId, currentBranchId)));
    }

    @GetMapping("/realtime/recent-orders")
    public ResponseEntity<List<Map<String, Object>>> recentOrders(
            @RequestParam(required = false) UUID branchId,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reportService.recentOrders(resolveBranchId(branchId, currentBranchId), limit));
    }

    /**
     * GET /api/v1/reports/export - Step 6-8 of UC09 main flow
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam String type, // revenue | inventory | staff
            @RequestParam String format, // excel | pdf
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Object exported = reportService.export(type, format, from, to);
        byte[] content = exported instanceof byte[] bytes ? bytes : new byte[0];
        String normalizedFormat = "xlsx".equalsIgnoreCase(format) ? "excel" : format.toLowerCase();
        String extension = "pdf".equals(normalizedFormat) ? "pdf" : "xlsx";
        MediaType mediaType = "pdf".equals(normalizedFormat)
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + type.toLowerCase() + "-report." + extension + "\"")
                .body(content);
    }

    @PostMapping("/schedule")
    public ResponseEntity<ReportScheduleResponse> schedule(
            @Valid @RequestBody CreateReportScheduleRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Branch-Id", required = false) UUID currentBranchId) {
        return ResponseEntity.ok(reportScheduleService.create(applyScheduleContext(request, userId, currentBranchId)));
    }

    @GetMapping("/schedules")
    public ResponseEntity<List<ReportScheduleResponse>> schedules() {
        return ResponseEntity.ok(reportScheduleService.list());
    }

    /**
     * TICKET-305: POST /reports/export/excel - queue an Excel export.
     * Returns a job descriptor ({@code status=queued}) instead of the file
     * itself; the future async job will materialise the .xlsx and serve it
     * at {@code /reports/export/download/{jobId}}.
     */
    @PostMapping("/export/excel")
    public ResponseEntity<Map<String, Object>> exportExcel(
            @Valid @RequestBody ReportExportRequest request) {
        return ResponseEntity.accepted().body(excelExportService.queueExcelExport(request));
    }

    /**
     * TICKET-306: POST /reports/export/pdf - queue a PDF export. Same
     * contract as {@link #exportExcel(ReportExportRequest)} but format=pdf.
     */
    @PostMapping("/export/pdf")
    public ResponseEntity<Map<String, Object>> exportPdf(
            @Valid @RequestBody ReportExportRequest request) {
        return ResponseEntity.accepted().body(pdfExportService.queuePdfExport(request));
    }

    /**
     * TICKET-306: DELETE /reports/schedules/{id} - cancel a scheduled report
     * by flipping {@code active=false}. Idempotent.
     */
    @DeleteMapping("/schedules/{id}")
    public ResponseEntity<ReportScheduleResponse> cancelSchedule(@PathVariable UUID id) {
        return ResponseEntity.ok(reportScheduleService.cancel(id));
    }

    private UUID resolveBranchId(UUID requestedBranchId, UUID currentBranchId) {
        return requestedBranchId != null ? requestedBranchId : currentBranchId;
    }

    private CreateReportScheduleRequest applyScheduleContext(CreateReportScheduleRequest request,
            UUID userId,
            UUID currentBranchId) {
        UUID createdBy = request.createdBy() != null ? request.createdBy() : userId;
        if (createdBy == null) {
            throw new InvalidOperationException(
                    "Schedule creator is required",
                    "Thiếu thông tin người tạo lịch báo cáo");
        }
        return new CreateReportScheduleRequest(
                request.type(),
                request.format(),
                resolveBranchId(request.branchId(), currentBranchId),
                request.cronExpression(),
                request.recipientEmail(),
                createdBy);
    }
}
