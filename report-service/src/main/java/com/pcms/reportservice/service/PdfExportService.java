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
 * TICKET-306: Stub for PDF report generation.
 *
 * <p>Same contract as {@link ExcelExportService} - returns a "queued" status
 * with a jobId and a (future) download URL. A real OpenPDF/iText renderer
 * is planned for a follow-up sprint.
 */
@Service
public class PdfExportService {

    private static final Logger log = LoggerFactory.getLogger(PdfExportService.class);
    private static final long DOWNLOAD_TTL_HOURS = 24L;

    public Map<String, Object> queuePdfExport(ReportExportRequest request) {
        if (request == null || request.reportType() == null || request.reportType().isBlank()) {
            throw new IllegalArgumentException("reportType is required");
        }
        String jobId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(DOWNLOAD_TTL_HOURS, ChronoUnit.HOURS);

        log.info("Queued PDF export jobId={} reportType={} branchId={} from={} to={}",
                jobId, request.reportType(), request.branchId(),
                request.fromDate(), request.toDate());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", "queued");
        payload.put("jobId", jobId);
        payload.put("format", "pdf");
        payload.put("reportType", request.reportType());
        payload.put("downloadUrl", "/reports/export/download/" + jobId);
        payload.put("queuedAt", now.toString());
        payload.put("expiresAt", expiresAt.toString());
        return payload;
    }
}
