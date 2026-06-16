package com.pcms.prescriptionservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.prescriptionservice.dto.CreatePrescriptionRequest;
import com.pcms.common.dto.PageResponse;
import com.pcms.prescriptionservice.dto.PrescriptionItemRequest;
import com.pcms.prescriptionservice.dto.PrescriptionResponse;
import com.pcms.prescriptionservice.dto.UpdatePrescriptionRequest;
import com.pcms.prescriptionservice.entity.Prescription;
import com.pcms.prescriptionservice.enums.PrescriptionStatus;
import com.pcms.prescriptionservice.repository.PrescriptionRepository;
import com.pcms.prescriptionservice.service.PrescriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PrescriptionResponse> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Prescription> result = prescriptionRepository.findAll(pageable);
        List<PrescriptionResponse> mapped = result.getContent().stream()
                .map(PrescriptionServiceImpl::toResponse)
                .toList();
        return PageResponse.of(result, PrescriptionServiceImpl::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getById(UUID id) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", id));
        return toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getByCode(String code) {
        Prescription p = prescriptionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", code));
        return toResponse(p);
    }

    @Override
    public PrescriptionResponse create(CreatePrescriptionRequest request) {
        validate(request);

        Prescription p = new Prescription();
        p.setPatientId(request.patientId());
        p.setDoctorId(request.doctorId());
        p.setDiagnosis(request.diagnosis());
        p.setNotes(request.notes());
        p.setLicenseNo(request.licenseNo());
        p.setCode(generateCode());

        boolean asDraft = Boolean.TRUE.equals(request.saveAsDraft());
        if (asDraft) {
            p.setStatus(PrescriptionStatus.DRAFT);
            // signature is still required by entity, generate a placeholder
            p.setSignatureHash(generateSignatureHash(p));
        } else {
            p.setStatus(PrescriptionStatus.SIGNED);
            p.setIssuedAt(LocalDateTime.now());
            p.setSignatureHash(generateSignatureHash(p));
        }

        Prescription saved = prescriptionRepository.save(p);
        return toResponse(saved, request.items());
    }

    @Override
    public PrescriptionResponse sign(UUID id) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", id));
        if (p.getStatus() == PrescriptionStatus.SIGNED) {
            return toResponse(p);
        }
        if (p.getStatus() == PrescriptionStatus.CANCELLED) {
            throw new InvalidOperationException(
                    "Cannot sign a cancelled prescription",
                    "Không thể ký đơn thuốc đã bị hủy");
        }
        if (p.getStatus() != PrescriptionStatus.DRAFT) {
            throw new InvalidOperationException(
                    "Only DRAFT can be signed",
                    "Chỉ có thể ký đơn ở trạng thái NHÁP");
        }
        p.setStatus(PrescriptionStatus.SIGNED);
        p.setIssuedAt(LocalDateTime.now());
        p.setSignatureHash(generateSignatureHash(p));
        Prescription saved = prescriptionRepository.save(p);
        return toResponse(saved);
    }

    @Override
    public PrescriptionResponse update(UUID id, UpdatePrescriptionRequest request) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", id));
        if (p.getStatus() != PrescriptionStatus.DRAFT) {
            throw new InvalidOperationException(
                    "Only DRAFT prescriptions can be updated",
                    "Chỉ có thể cập nhật đơn ở trạng thái NHÁP");
        }
        p.setDiagnosis(request.diagnosis());
        p.setNotes(request.notes());
        Prescription saved = prescriptionRepository.save(p);
        return toResponse(saved, request.items());
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse print(UUID id) {
        return getById(id);
    }

    private void validate(CreatePrescriptionRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new InvalidOperationException(
                    "At least one medicine line is required",
                    "Phải có ít nhất một dòng thuốc");
        }
        if (request.licenseNo() == null || request.licenseNo().isBlank()) {
            throw new InvalidOperationException(
                    "License number is required",
                    "Số giấy phép là bắt buộc");
        }
        if (request.patientId() == null) {
            throw new InvalidOperationException(
                    "Patient is required",
                    "Bệnh nhân là bắt buộc");
        }
    }

    private String generateCode() {
        String year = String.valueOf(LocalDate.now().getYear());
        Pageable limit = PageRequest.of(0, 1);
        List<Prescription> latest = prescriptionRepository.findByYearPrefix(year, limit);
        int nextNum = 1;
        if (!latest.isEmpty()) {
            Optional<Prescription> first = latest.stream().findFirst();
            if (first.isPresent()) {
                String code = first.get().getCode();
                String numPart = code.substring(code.lastIndexOf('-') + 1);
                try {
                    nextNum = Integer.parseInt(numPart) + 1;
                } catch (NumberFormatException ignored) {
                    // keep nextNum=1
                }
            }
        }
        return String.format("RX-%s%04d", year, nextNum);
    }

    private String generateSignatureHash(Prescription p) {
        String raw = p.getCode() + "|" + p.getPatientId() + "|" + p.getDoctorId() + "|" + p.getDiagnosis();
        return UUID.nameUUIDFromBytes(raw.getBytes()).toString().replace("-", "");
    }

    private static PrescriptionResponse toResponse(Prescription p) {
        return toResponse(p, List.of());
    }

    private static PrescriptionResponse toResponse(Prescription p, List<PrescriptionItemRequest> items) {
        return new PrescriptionResponse(
                p.getId(),
                p.getCode(),
                p.getPatientId(),
                p.getDoctorId(),
                p.getDiagnosis(),
                p.getNotes(),
                p.getSignatureHash(),
                p.getStatus(),
                p.getIssuedAt(),
                items
        );
    }
}
