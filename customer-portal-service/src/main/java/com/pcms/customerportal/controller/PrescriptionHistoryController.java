package com.pcms.customerportal.controller;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.response.PrescriptionSummaryResponse;
import com.pcms.customerportal.security.CurrentUser;
import com.pcms.customerportal.service.PrescriptionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * TICKET-705 - B2C prescription history.
 * <p>Two endpoints: list mine (with pagination) and re-download a single
 * PDF (with ownership check). Both call the prescription-service via Feign
 * and never trust a client-supplied patientId - it's always substituted
 * with the JWT-derived current customer id.
 */
@RestController
@RequestMapping("/prescriptions")
@Tag(name = "UC14 - Customer Account / Prescription History")
public class PrescriptionHistoryController {

    private final PrescriptionHistoryService service;

    public PrescriptionHistoryController(PrescriptionHistoryService service) {
        this.service = service;
    }

    @GetMapping("/me")
    @Operation(summary = "List my prescription history (paginated)")
    public ResponseEntity<PageResponse<PrescriptionSummaryResponse>> listMine(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                service.listMine(CurrentUser.requireCustomerId(userId), page, size));
    }

    @GetMapping("/{id}/re-download")
    @Operation(summary = "Re-download prescription PDF (owner only)")
    public ResponseEntity<byte[]> reDownload(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable("id") UUID id) {
        ResponseEntity<byte[]> upstream = service.reDownload(
                CurrentUser.requireCustomerId(userId), id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "prescription-" + id + ".pdf");
        return new ResponseEntity<>(upstream.getBody(), headers, upstream.getStatusCode());
    }
}
