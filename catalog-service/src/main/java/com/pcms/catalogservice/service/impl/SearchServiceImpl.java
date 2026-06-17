package com.pcms.catalogservice.service.impl;

import com.pcms.catalogservice.client.InventoryClient;
import com.pcms.catalogservice.dto.response.MedicineResponse;
import com.pcms.catalogservice.entity.Medicine;
import com.pcms.catalogservice.repository.MedicineRepository;
import com.pcms.catalogservice.service.SearchService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    private final MedicineRepository medicineRepository;
    private final InventoryClient inventoryClient;

    public SearchServiceImpl(MedicineRepository medicineRepository, InventoryClient inventoryClient) {
        this.medicineRepository = medicineRepository;
        this.inventoryClient = inventoryClient;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "medicineAutocomplete", key = "#q == null ? '' : #q.toLowerCase()")
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
    @Cacheable(value = "medicineFullSearch", condition = "#inStock == null")
    public List<MedicineResponse> fullSearch(String q,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock) {
        List<Medicine> results = medicineRepository
                .search(q, categoryId, minPrice, maxPrice,
                        com.pcms.catalogservice.enums.MedicineStatus.ACTIVE,
                        PageRequest.of(0, 100))
                .getContent();
        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        List<Medicine> filteredResults = applyInStockFilter(results, inStock);
        if (filteredResults.isEmpty()) {
            return Collections.emptyList();
        }
        return filteredResults.stream().map(MedicineResponse::from).toList();
    }

    private List<Medicine> applyInStockFilter(List<Medicine> medicines, Boolean inStock) {
        if (inStock == null) {
            return medicines;
        }

        Set<UUID> medicineIdsWithStock = inventoryClient.getInventory(null).stream()
                .filter(this::hasAvailableStock)
                .map(this::extractMedicineId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return medicines.stream()
                .filter(medicine -> Boolean.TRUE.equals(inStock) == medicineIdsWithStock.contains(medicine.getId()))
                .toList();
    }

    private boolean hasAvailableStock(Map<String, Object> batch) {
        Object quantity = batch.getOrDefault("qtyOnHand", batch.get("quantity"));
        if (quantity instanceof Number number) {
            return number.intValue() > 0;
        }
        if (quantity instanceof String text) {
            try {
                return Integer.parseInt(text) > 0;
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        return false;
    }

    private UUID extractMedicineId(Map<String, Object> batch) {
        Object medicineId = batch.get("medicineId");
        if (medicineId instanceof UUID uuid) {
            return uuid;
        }
        if (medicineId instanceof String text && !text.isBlank()) {
            return UUID.fromString(text);
        }
        return null;
    }
}
