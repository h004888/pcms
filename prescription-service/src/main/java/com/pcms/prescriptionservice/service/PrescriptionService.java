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

    /**
     * Print/export a prescription in printable form (HTML/JSON placeholder).
     */
    PrescriptionResponse print(UUID id);
}
