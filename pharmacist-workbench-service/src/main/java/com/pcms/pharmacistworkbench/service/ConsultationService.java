package com.pcms.pharmacistworkbench.service;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.pharmacistworkbench.dto.request.StartConsultationRequest;
import com.pcms.pharmacistworkbench.dto.response.ConsultationResponse;
import com.pcms.pharmacistworkbench.entity.Consultation;
import com.pcms.pharmacistworkbench.repository.ConsultationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConsultationService {

    private final ConsultationRepository repository;

    public ConsultationService(ConsultationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ConsultationResponse start(StartConsultationRequest request, UUID pharmacistId) {
        Consultation c = new Consultation();
        c.setCustomerId(request.customerId());
        c.setPharmacistId(pharmacistId);
        c.setChannel(request.channel().toUpperCase());
        c.setStatus("ACTIVE");
        c.setStartedAt(LocalDateTime.now());
        c = repository.save(c);
        return toResponse(c);
    }

    @Transactional
    public ConsultationResponse end(UUID consultationId) {
        Consultation c = repository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", consultationId));
        c.setStatus("ENDED");
        c.setEndedAt(LocalDateTime.now());
        return toResponse(repository.save(c));
    }

    @Transactional
    public ConsultationResponse appendMessage(UUID consultationId, String message) {
        Consultation c = repository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", consultationId));
        String current = c.getTranscript() != null ? c.getTranscript() : "";
        String newMsg = LocalDateTime.now() + ": " + message + "\n";
        c.setTranscript(current + newMsg);
        return toResponse(repository.save(c));
    }

    @Transactional(readOnly = true)
    public ConsultationResponse get(UUID id) {
        Consultation c = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", id));
        return toResponse(c);
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> listByCustomer(UUID customerId) {
        return repository.findByCustomerIdOrderByStartedAtDesc(customerId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> listByPharmacist(UUID pharmacistId) {
        return repository.findByPharmacistIdAndStatusOrderByStartedAtDesc(pharmacistId, "ACTIVE").stream()
                .map(this::toResponse).toList();
    }

    private ConsultationResponse toResponse(Consultation c) {
        return new ConsultationResponse(c.getId(), c.getCustomerId(), c.getPharmacistId(),
                c.getChannel(), c.getStatus(), c.getStartedAt(), c.getEndedAt());
    }
}
