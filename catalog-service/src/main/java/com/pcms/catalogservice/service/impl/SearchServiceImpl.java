package com.pcms.catalogservice.service.impl;

import com.pcms.catalogservice.dto.response.MedicineResponse;
import com.pcms.catalogservice.entity.Medicine;
import com.pcms.catalogservice.repository.MedicineRepository;
import com.pcms.catalogservice.service.SearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class SearchServiceImpl implements SearchService {

    private final MedicineRepository medicineRepository;

    public SearchServiceImpl(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> autocomplete(String q) {
        if (q == null || q.isBlank())
            return Collections.emptyList();
        List<Medicine> results = medicineRepository.findTop5ByNameLike(q, PageRequest.of(0, 5));
        if (results.isEmpty())
            return Collections.emptyList();
        return results.stream().map(MedicineResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> fullSearch(String q,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock) {
        // inStock is accepted for API compatibility with SCR-SEARCH.
        // Stock lives in inventory-service, so this dev implementation ignores it.
        List<Medicine> results = medicineRepository
                .search(q, categoryId, minPrice, maxPrice,
                        com.pcms.catalogservice.enums.MedicineStatus.ACTIVE,
                        PageRequest.of(0, 100))
                .getContent();
        if (results.isEmpty())
            return Collections.emptyList();
        return results.stream().map(MedicineResponse::from).toList();
    }
}
