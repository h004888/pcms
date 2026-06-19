package com.pcms.pharmacistworkbench.service;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.pharmacistworkbench.dto.request.MarkVipRequest;
import com.pcms.pharmacistworkbench.dto.response.VipMarkResponse;
import com.pcms.pharmacistworkbench.entity.VipMark;
import com.pcms.pharmacistworkbench.repository.VipMarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VipMarkService {

    private final VipMarkRepository repository;

    public VipMarkService(VipMarkRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public VipMarkResponse mark(MarkVipRequest request, UUID actorId) {
        VipMark existing = repository.findByCustomerId(request.customerId()).orElse(null);
        if (existing != null) {
            existing.setTier(request.tier());
            existing.setReason(request.reason());
            existing.setMarkedBy(actorId);
            return toResponse(repository.save(existing));
        }
        VipMark v = new VipMark();
        v.setCustomerId(request.customerId());
        v.setMarkedBy(actorId);
        v.setTier(request.tier());
        v.setReason(request.reason());
        v.setLoyaltyScore(50); // default score when newly marked
        return toResponse(repository.save(v));
    }

    @Transactional
    public void unmark(UUID customerId) {
        VipMark v = repository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("VipMark", customerId));
        repository.delete(v);
    }

    @Transactional(readOnly = true)
    public VipMarkResponse getByCustomer(UUID customerId) {
        VipMark v = repository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("VipMark", customerId));
        return toResponse(v);
    }

    @Transactional(readOnly = true)
    public List<VipMarkResponse> listByTier(String tier) {
        return repository.findByTierOrderByLoyaltyScoreDesc(tier).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<VipMarkResponse> listAll() {
        return repository.findAllByOrderByLoyaltyScoreDesc().stream()
                .map(this::toResponse).toList();
    }

    private VipMarkResponse toResponse(VipMark v) {
        return new VipMarkResponse(v.getCustomerId(), v.getMarkedBy(), v.getTier(),
                v.getLoyaltyScore(), v.getLifetimeSpend(), v.getMarkedAt(), v.getReason());
    }
}
