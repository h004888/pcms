package com.pcms.customerportal.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.request.AddFavoriteRequest;
import com.pcms.customerportal.dto.response.FavoriteResponse;
import com.pcms.customerportal.entity.CustomerFavorite;
import com.pcms.customerportal.repository.CustomerFavoriteRepository;
import com.pcms.customerportal.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteServiceImpl.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final CustomerFavoriteRepository repo;

    public FavoriteServiceImpl(CustomerFavoriteRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FavoriteResponse> list(UUID currentCustomerId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Page<CustomerFavorite> p = repo.findByCustomerIdOrderByCreatedAtDesc(
                currentCustomerId, PageRequest.of(safePage, safeSize));
        return PageResponse.of(p, FavoriteResponse::from);
    }

    @Override
    @Transactional
    public FavoriteResponse add(UUID currentCustomerId, AddFavoriteRequest req) {
        // Idempotent: if already favorited, return existing
        Optional<CustomerFavorite> existing = repo.findByCustomerIdAndMedicineId(
                currentCustomerId, req.medicineId());
        if (existing.isPresent()) {
            log.debug("[fav] already favorited customer={} medicine={}", currentCustomerId, req.medicineId());
            return FavoriteResponse.from(existing.get());
        }

        CustomerFavorite f = new CustomerFavorite();
        f.setCustomerId(currentCustomerId);
        f.setMedicineId(req.medicineId());
        CustomerFavorite saved = repo.save(f);
        log.info("[fav] added customer={} medicine={}", currentCustomerId, req.medicineId());
        return FavoriteResponse.from(saved);
    }

    @Override
    @Transactional
    public void remove(UUID currentCustomerId, UUID medicineId) {
        long deleted = repo.deleteByCustomerIdAndMedicineId(currentCustomerId, medicineId);
        if (deleted == 0) {
            log.debug("[fav] remove no-op (not favorited) customer={} medicine={}",
                    currentCustomerId, medicineId);
            return;
        }
        log.info("[fav] removed customer={} medicine={}", currentCustomerId, medicineId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(UUID currentCustomerId, UUID medicineId) {
        return repo.existsByCustomerIdAndMedicineId(currentCustomerId, medicineId);
    }
}
