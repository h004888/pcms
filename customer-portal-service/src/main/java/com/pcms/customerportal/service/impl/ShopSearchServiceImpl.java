package com.pcms.customerportal.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.client.CatalogClient;
import com.pcms.customerportal.repository.HerbRepository;
import com.pcms.customerportal.repository.IngredientRepository;
import com.pcms.customerportal.service.ShopSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation of ShopSearchService.
 *
 * <p>Today: text search only via catalog-service Feign.
 * Tomorrow: add ai-engine-service Feign for semantic search (Sprint 8).
 *
 * <p>Lookup endpoints for ingredient/herb query local tables.
 */
@Service
public class ShopSearchServiceImpl implements ShopSearchService {

    private static final Logger log = LoggerFactory.getLogger(ShopSearchServiceImpl.class);

    private final CatalogClient catalogClient;
    private final IngredientRepository ingredientRepo;
    private final HerbRepository herbRepo;

    public ShopSearchServiceImpl(CatalogClient catalogClient,
                                 IngredientRepository ingredientRepo,
                                 HerbRepository herbRepo) {
        this.catalogClient = catalogClient;
        this.ingredientRepo = ingredientRepo;
        this.herbRepo = herbRepo;
    }

    @Override
    public PageResponse<Map<String, Object>> search(String q, int page, int size) {
        List<Map<String, Object>> raw = catalogClient.searchMedicines(q, page, size);
        return toPageResponse(raw, page, size);
    }

    @Override
    public PageResponse<Map<String, Object>> lookupDrug(String q, String aToZ, int page, int size) {
        // Reuse catalog search but apply client-side A-Z filter on first letter of name.
        PageResponse<Map<String, Object>> all = search(q, page, size);
        if (aToZ == null || aToZ.isBlank() || aToZ.length() != 1) {
            return all;
        }
        char letter = Character.toUpperCase(aToZ.charAt(0));
        List<Map<String, Object>> filtered = all.data().stream()
                .filter(m -> {
                    Object name = m.get("name");
                    return name != null && !name.toString().isEmpty()
                            && Character.toUpperCase(name.toString().charAt(0)) == letter;
                })
                .toList();
        return new PageResponse<>(filtered, page, size, filtered.size(),
                (int) Math.ceil((double) filtered.size() / size));
    }

    @Override
    public PageResponse<Map<String, Object>> lookupIngredient(String q, int page, int size) {
        try {
            var pageable = PageRequest.of(page, size);
            var p = ingredientRepo.searchByName(q, pageable);
            return PageResponse.of(p, i -> Map.<String, Object>of(
                    "id", i.getId().toString(),
                    "nameVi", i.getNameVi() != null ? i.getNameVi() : "",
                    "nameEn", i.getNameEn() != null ? i.getNameEn() : "",
                    "synonyms", i.getSynonyms() != null ? i.getSynonyms() : ""
            ));
        } catch (Exception e) {
            log.warn("lookupIngredient failed: {}", e.getMessage());
            return PageResponse.empty(page, size);
        }
    }

    @Override
    public PageResponse<Map<String, Object>> lookupHerb(String q, int page, int size) {
        try {
            var pageable = PageRequest.of(page, size);
            var p = herbRepo.searchByName(q, pageable);
            return PageResponse.of(p, h -> Map.<String, Object>of(
                    "id", h.getId().toString(),
                    "nameVi", h.getNameVi() != null ? h.getNameVi() : "",
                    "nameEn", h.getNameEn() != null ? h.getNameEn() : "",
                    "traditionalUse", h.getTraditionalUse() != null ? h.getTraditionalUse() : "",
                    "imageUrl", h.getImageUrl() != null ? h.getImageUrl() : ""
            ));
        } catch (Exception e) {
            log.warn("lookupHerb failed: {}", e.getMessage());
            return PageResponse.empty(page, size);
        }
    }

    @SuppressWarnings("unchecked")
    private PageResponse<Map<String, Object>> toPageResponse(List<Map<String, Object>> data, int page, int size) {
        long total = data.size();
        int totalPages = (int) Math.ceil((double) total / Math.max(size, 1));
        return new PageResponse<>(data, page, size, total, totalPages);
    }
}