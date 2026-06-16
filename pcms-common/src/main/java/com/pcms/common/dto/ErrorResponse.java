package com.pcms.common.dto;

import com.pcms.common.correlation.CorrelationContext;

import java.time.Instant;
import java.util.Map;

/**
 * Standard error envelope used by all PCMS services.
 * Aligned with SRS v1.3.0 Section 6.16 (RFC 7807-inspired) and the MSG01-MSG34 catalog.
 *
 * <p>Correlation id is automatically populated by {@code GlobalExceptionHandler}
 * from MDC / {@code X-Correlation-Id} header (CR-06).
 *
 * Fields:
 * - code          : MSG01..MSG34 stable contract
 * - status        : HTTP status code (echoed for client convenience)
 * - message       : English message (default)
 * - messageVi     : Vietnamese message (i18n-ready, CR-01)
 * - details       : optional structured details (field errors, lockedUntil, etc.)
 * - path          : request URI (for debugging)
 * - correlationId : correlation id from CR-06 (auto-filled by GlobalExceptionHandler)
 * - timestamp     : server-side UTC instant
 */
public record ErrorResponse(
        String code,
        int status,
        String message,
        String messageVi,
        Map<String, Object> details,
        String path,
        String correlationId,
        Instant timestamp
) {
    public static ErrorResponse of(String code, int status, String message) {
        return new ErrorResponse(code, status, message, null, null, null, null, Instant.now());
    }

    public static ErrorResponse of(String code, int status, String message, String messageVi) {
        return new ErrorResponse(code, status, message, messageVi, null, null, null, Instant.now());
    }

    public static ErrorResponse of(String code, int status, String message, Map<String, Object> details) {
        return new ErrorResponse(code, status, message, null, details, null, null, Instant.now());
    }

    public static ErrorResponse of(String code, int status, String message, String messageVi, Map<String, Object> details) {
        return new ErrorResponse(code, status, message, messageVi, details, null, null, Instant.now());
    }
}
