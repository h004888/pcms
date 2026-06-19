package com.pcms.customerportal.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign client for prescription-service.
 * <p>Used by TICKET-705 to expose the customer's prescription history in
 * the B2C portal. Resilience4j circuit-breaker is configured to fail
 * fast if prescription-service is down (return empty page / 404 blob).
 */
@FeignClient(name = "prescription-service")
public interface PrescriptionClient {

    Logger log = LoggerFactory.getLogger(PrescriptionClient.class);

    /**
     * GET /api/v1/prescriptions?patientId=...&page=0&size=20
     * Returns a page response with prescription summaries. We receive
     * Map for flexibility (typed DTO can be added once contract stabilizes).
     */
    @GetMapping("/prescriptions")
    @CircuitBreaker(name = "prescriptionService", fallbackMethod = "fallbackListByPatient")
    Map<String, Object> listByPatient(
            @RequestParam(name = "patientId") String patientId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size);

    /**
     * GET /api/v1/prescriptions/{id}
     * Returns a single prescription record (used for ownership check + PDF).
     */
    @GetMapping("/prescriptions/{id}")
    @CircuitBreaker(name = "prescriptionService", fallbackMethod = "fallbackGetById")
    Map<String, Object> getById(@PathVariable("id") String id);

    /**
     * GET /api/v1/prescriptions/{id}/print
     * Returns a PDF blob (Sprint 3 added this endpoint). Used for
     * "re-download" by the customer.
     */
    @GetMapping("/prescriptions/{id}/print")
    @CircuitBreaker(name = "prescriptionService", fallbackMethod = "fallbackPrint")
    ResponseEntity<byte[]> printPdf(@PathVariable("id") String id);

    // ====================================================================
    // Fallback methods
    // ====================================================================

    default Map<String, Object> fallbackListByPatient(String patientId, int page, int size, Throwable t) {
        log.warn("prescription-service unavailable while listing for patient {}: {}",
                patientId, t.getMessage());
        return Map.of("data", java.util.List.of(),
                      "page", page, "size", size, "total", 0, "totalPages", 0);
    }

    default Map<String, Object> fallbackGetById(String id, Throwable t) {
        log.warn("prescription-service unavailable while fetching {}: {}", id, t.getMessage());
        return Map.of();
    }

    default ResponseEntity<byte[]> fallbackPrint(String id, Throwable t) {
        log.warn("prescription-service unavailable while printing {}: {}", id, t.getMessage());
        return ResponseEntity.status(503).body(new byte[0]);
    }
}
