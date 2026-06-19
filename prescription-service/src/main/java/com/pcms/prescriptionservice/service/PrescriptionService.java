package com.pcms.prescriptionservice.service;

import com.pcms.prescriptionservice.dto.CreatePrescriptionRequest;
import com.pcms.common.dto.PageResponse;
import com.pcms.prescriptionservice.dto.PrescriptionResponse;
import com.pcms.prescriptionservice.dto.UpdatePrescriptionRequest;

import java.util.UUID;

public interface PrescriptionService {

    PageResponse<PrescriptionResponse> list(int page, int size);

    PrescriptionResponse getById(UUID id);

    PrescriptionResponse getByCode(String code);

    PrescriptionResponse create(CreatePrescriptionRequest request);

    PrescriptionResponse sign(UUID id);

    PrescriptionResponse update(UUID id, UpdatePrescriptionRequest request);

    /** Link a signed prescription to an order after POS sale. */
    PrescriptionResponse linkOrder(UUID id, UUID orderId);

    /**
     * Print/export a prescription in printable form (HTML/JSON placeholder).
     */
    PrescriptionResponse print(UUID id);

    /**
     * TICKET-303: Cancel a prescription (soft-delete).
     * Allowed only if {@code status = DRAFT} or the prescription has not yet
     * been linked to a PAID order. Throws {@code BusinessException} with
     * MSG19 otherwise (caller may need to re-print the underlying message).
     */
    PrescriptionResponse cancel(UUID id, UUID actorId);
}
