package com.pcms.prescriptionservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.prescriptionservice.client.CustomerClient;
import com.pcms.prescriptionservice.client.UserClient;
import com.pcms.prescriptionservice.dto.CreatePrescriptionRequest;
import com.pcms.common.dto.PageResponse;
import com.pcms.prescriptionservice.dto.PrescriptionItemRequest;
import com.pcms.prescriptionservice.dto.PrescriptionResponse;
import com.pcms.prescriptionservice.dto.UpdatePrescriptionRequest;
import com.pcms.prescriptionservice.entity.Prescription;
import com.pcms.prescriptionservice.enums.PrescriptionStatus;
import com.pcms.prescriptionservice.repository.PrescriptionRepository;
import com.pcms.prescriptionservice.service.PrescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionServiceImpl.class);

    private final PrescriptionRepository prescriptionRepository;
    private final UserClient userClient;
    private final CustomerClient customerClient;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository,
            UserClient userClient,
            CustomerClient customerClient) {
        this.prescriptionRepository = prescriptionRepository;
        this.userClient = userClient;
        this.customerClient = customerClient;
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
    public PrescriptionResponse linkOrder(UUID id, UUID orderId) {
        if (orderId == null) {
            throw new InvalidOperationException(
                    "Order ID is required",
                    "Mã đơn hàng là bắt buộc");
        }
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", id));
        if (prescription.getStatus() != PrescriptionStatus.SIGNED) {
            throw new InvalidOperationException(
                    "Only signed prescriptions can be linked to orders",
                    "Chỉ có thể liên kết đơn thuốc đã ký với đơn hàng");
        }
        if (prescription.getOrderId() != null && !prescription.getOrderId().equals(orderId)) {
            throw new InvalidOperationException(
                    "Prescription is already linked to another order",
                    "Đơn thuốc đã được liên kết với đơn hàng khác");
        }
        prescription.setOrderId(orderId);
        return toResponse(prescriptionRepository.save(prescription));
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse print(UUID id) {
        return getById(id);
    }

    @Override
    public PrescriptionResponse cancel(UUID id, UUID actorId) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", id));

        // Already cancelled - idempotent: return current state.
        if (p.getStatus() == PrescriptionStatus.CANCELLED) {
            return toResponse(p);
        }

        // Allow cancel when still a DRAFT.
        if (p.getStatus() == PrescriptionStatus.DRAFT) {
            p.setStatus(PrescriptionStatus.CANCELLED);
            log.info("Prescription {} cancelled (DRAFT) by actor={}", id, actorId);
            return toResponse(prescriptionRepository.save(p));
        }

        // SIGNED prescriptions: only allow cancel if not yet linked to any order.
        // If an order is linked, refuse to cancel (downstream order may have
        // already been PAID; cancelling here would break the audit trail).
        if (p.getStatus() == PrescriptionStatus.SIGNED) {
            if (p.getOrderId() == null) {
                p.setStatus(PrescriptionStatus.CANCELLED);
                log.info("Prescription {} cancelled (SIGNED, no order) by actor={}", id, actorId);
                return toResponse(prescriptionRepository.save(p));
            }
            // Linked to an order - block with MSG19 (re-use the cancel-conflict
            // message from the order UC). The orderId is included in the message
            // for the client to display.
            throw new InvalidOperationException(
                    "Cannot cancel a prescription linked to an order (orderId=" + p.getOrderId() + ")",
                    "Không thể huỷ đơn thuốc đã liên kết với đơn hàng (mã đơn: " + p.getOrderId() + "). Vui lòng xử lý qua đơn hàng.",
                    409);
        }

        // Defensive fallback (should be unreachable).
        throw new InvalidOperationException(
                "Cannot cancel prescription in status " + p.getStatus(),
                "Không thể huỷ đơn thuốc ở trạng thái " + p.getStatus());
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
        if (request.doctorId() == null) {
            throw new InvalidOperationException(
                    "Doctor is required",
                    "Bác sĩ/dược sĩ kê đơn là bắt buộc");
        }
        validatePatient(request.patientId());
        validateDoctor(request.doctorId());
    }

    private void validatePatient(UUID patientId) {
        try {
            Map<String, Object> customer = customerClient.getCustomerById(patientId);
            if (customer == null || "UNREACHABLE".equals(customer.get("status"))) {
                throw new ResourceNotFoundException("Customer", patientId);
            }
            if (!"ACTIVE".equalsIgnoreCase(String.valueOf(customer.get("status")))) {
                throw new InvalidOperationException(
                        "Patient customer must be active",
                        "Khách hàng bệnh nhân phải đang hoạt động");
            }
        } catch (feign.FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Customer", patientId);
        }
    }

    private void validateDoctor(UUID doctorId) {
        try {
            Map<String, Object> user = userClient.getUserById(doctorId);
            if (user == null || "UNREACHABLE".equals(user.get("status"))) {
                throw new ResourceNotFoundException("User", doctorId);
            }
            String role = String.valueOf(user.get("role"));
            if (!"PHARMACIST".equalsIgnoreCase(role) && !"BRANCH_MANAGER".equalsIgnoreCase(role)) {
                throw new InvalidOperationException(
                        "Prescription signer must be PHARMACIST or BRANCH_MANAGER",
                        "Người kê đơn phải là dược sĩ hoặc quản lý chi nhánh");
            }
            if (!"ACTIVE".equalsIgnoreCase(String.valueOf(user.get("status")))) {
                throw new InvalidOperationException(
                        "Prescription signer must be active",
                        "Người kê đơn phải đang hoạt động");
            }
        } catch (feign.FeignException.NotFound ex) {
            throw new ResourceNotFoundException("User", doctorId);
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
                p.getOrderId(),
                p.getDiagnosis(),
                p.getNotes(),
                p.getSignatureHash(),
                p.getStatus(),
                p.getIssuedAt(),
                items);
    }
}
