package com.pcms.reportservice.service;

import com.pcms.reportservice.dto.ReportExportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TICKET-305: Stub for Excel (xlsx) report generation.
 *
 * <p>This implementation returns a "queued" response with a download URL that
 * a future async job can fulfil. The plan calls for a real Apache POI
 * renderer in a follow-up sprint; the contract (request shape, response
 * shape, async-queue semantics) is stable so the frontend can integrate
 * against it now.
 *
 * <p>Response payload:
 * <pre>{@code
 * {
 *   "status":     "queued",
 *   "jobId":      "uuid",
 *   "downloadUrl":"/reports/export/download/{jobId}",
 *   "format":     "excel",
 *   "reportType": "revenue|inventory|staff",
 *   "queuedAt":   "2026-06-19T03:00:00Z",
 *   "expiresAt":  "2026-06-20T03:00:00Z"
 * }
 * }</pre>
 */
@Service
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);
    private static final long DOWNLOAD_TTL_HOURS = 24L;

    /**
     * Queue an Excel export job and return a status map.
     */
    public Map<String, Object> queueExcelExport(ReportExportRequest request) {
        if (request == null || request.reportType() == null || request.reportType().isBlank()) {
            throw new IllegalArgumentException("reportType is required");
        }
        String jobId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(DOWNLOAD_TTL_HOURS, ChronoUnit.HOURS);

        log.info("Queued Excel export jobId={} reportType={} branchId={} from={} to={}",
                jobId, request.reportType(), request.branchId(),
                request.fromDate(), request.toDate());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "queued");
        payload.put("jobId", jobId);
        payload.put("format", "excel");
        payload.put("reportType", request.reportType());
        payload.put("downloadUrl", "/reports/export/download/" + jobId);
        payload.put("queuedAt", now.toString());
        payload.put("expiresAt", expiresAt.toString());
        return payload;
    }
}
