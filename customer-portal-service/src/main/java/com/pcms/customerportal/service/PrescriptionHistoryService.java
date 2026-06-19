package com.pcms.customerportal.service;

import com.pcms.common.dto.PageResponse;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.client.PrescriptionClient;
import com.pcms.customerportal.dto.response.PrescriptionSummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * TICKET-705 - B2C prescription history proxy.
 * <p>This is a thin facade over {@link PrescriptionClient}. We never store
 * prescriptions locally; we just decorate the upstream response with
 * ownership enforcement.
 */
@Service
public class PrescriptionHistoryService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionHistoryService.class);

    private final PrescriptionClient prescriptionClient;

    public PrescriptionHistoryService(PrescriptionClient prescriptionClient) {
        this.prescriptionClient = prescriptionClient;
    }

    /**
     * List prescriptions owned by the current customer.
     * Note: We do NOT expose the upstream {@code patientId} filter directly
     * to the caller - we substitute it with the JWT-derived id server-side.
     */
    @Transactional(readOnly = true)
    public PageResponse<PrescriptionSummaryResponse> listMine(UUID currentCustomerId, int page, int size) {
        Map<String, Object> raw = prescriptionClient.listByPatient(
                currentCustomerId.toString(), page, size);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) raw.getOrDefault("data", List.of());

        List<PrescriptionSummaryResponse> items = data.stream()
                .map(PrescriptionSummaryResponse::from)
                .toList();

        int p   = ((Number) raw.getOrDefault("page", page)).intValue();
        int s   = ((Number) raw.getOrDefault("size", size)).intValue();
        long t  = ((Number) raw.getOrDefault("total", items.size())).longValue();
        int tp  = ((Number) raw.getOrDefault("totalPages", 1)).intValue();

        return new PageResponse<>(items, p, s, t, tp);
    }

    /**
     * Re-download a prescription PDF. The customer must own the prescription.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> reDownload(UUID currentCustomerId, UUID prescriptionId) {
        Map<String, Object> rx = prescriptionClient.getById(prescriptionId.toString());
        if (rx == null || rx.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Prescription not found: " + prescriptionId,
                    "Không tìm thấy đơn thuốc: " + prescriptionId);
        }

        Object patientIdRaw = rx.get("patientId");
        if (patientIdRaw == null) {
            throw new ResourceNotFoundException(
                    "Prescription patientId missing",
                    "Đơn thuốc không có thông tin bệnh nhân");
        }
        UUID patientId = UUID.fromString(patientIdRaw.toString());
        if (!patientId.equals(currentCustomerId)) {
            log.warn("[rx-history] forbidden re-download attempt: customer={} rx={} owner={}",
                    currentCustomerId, prescriptionId, patientId);
            throw new InvalidOperationException(
                    "Prescription does not belong to current customer",
                    "Đơn thuốc không thuộc về khách hàng hiện tại");
        }

        ResponseEntity<byte[]> pdf = prescriptionClient.printPdf(prescriptionId.toString());
        if (!pdf.getStatusCode().is2xxSuccessful() || pdf.getBody() == null || pdf.getBody().length == 0) {
            throw new ResourceNotFoundException(
                    "Prescription PDF not available: " + prescriptionId,
                    "Không có sẵn file PDF cho đơn thuốc: " + prescriptionId);
        }
        log.info("[rx-history] re-download customer={} rx={} bytes={}",
                currentCustomerId, prescriptionId, pdf.getBody().length);
        return pdf;
    }
}
