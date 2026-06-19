package com.pcms.customerportal.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for GET /prescriptions/me.
 * Lightweight summary built from prescription-service Feign response.
 */
public record PrescriptionSummaryResponse(
        UUID id,
        String code,
        UUID patientId,
        UUID doctorId,
        UUID orderId,
        String diagnosis,
        String status,
        LocalDateTime issuedAt,
        List<Map<String, Object>> items
) {
    /** Build from a Map (raw Feign response) - defensive against missing fields. */
    @SuppressWarnings("unchecked")
    public static PrescriptionSummaryResponse from(Map<String, Object> m) {
        return new PrescriptionSummaryResponse(
                m.get("id") != null ? UUID.fromString(m.get("id").toString()) : null,
                str(m, "code"),
                m.get("patientId") != null ? UUID.fromString(m.get("patientId").toString()) : null,
                m.get("doctorId") != null ? UUID.fromString(m.get("doctorId").toString()) : null,
                m.get("orderId") != null ? UUID.fromString(m.get("orderId").toString()) : null,
                str(m, "diagnosis"),
                str(m, "status"),
                m.get("issuedAt") != null
                        ? LocalDateTime.parse(m.get("issuedAt").toString()) : null,
                (List<Map<String, Object>>) m.getOrDefault("items", List.of())
        );
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : null;
    }
}
