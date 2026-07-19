package com.pcms.customerportal.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.client.CatalogClient;
import com.pcms.customerportal.repository.HerbRepository;
import com.pcms.customerportal.repository.IngredientRepository;
import com.pcms.customerportal.service.ShopSearchService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
    public PageResponse<Map<String, Object>> search(String q, String category, int page, int size, String sort) {
        try {
            List<Map<String, Object>> raw = catalogClient.searchMedicines(q, page, size);
            // Filter by category name if specified
            if (category != null && !category.isBlank()) {
                raw = raw.stream()
                    .filter(m -> {
                        Object catId = m.get("categoryId");
                        return catId != null && catId.toString().equals(category);
                    })
                    .toList();
            }
            // Client-side sort
            if (sort != null && !sort.isBlank()) {
                Comparator<Map<String, Object>> cmp = switch (sort) {
                    case "price_asc"  -> Comparator.comparing(m -> toDouble(m.get("price")));
                    case "price_desc" -> Comparator.<Map<String, Object>, Double>comparing(m -> toDouble(m.get("price"))).reversed();
                    case "name_asc"   -> Comparator.comparing(m -> String.valueOf(m.getOrDefault("name", "")));
                    case "name_desc"  -> Comparator.<Map<String, Object>, String>comparing(m -> String.valueOf(m.getOrDefault("name", ""))).reversed();
                    default           -> null;
                };
                if (cmp != null) {
                    raw = raw.stream().sorted(cmp).toList();
                }
            }
            return toPageResponse(raw, page, size);
        } catch (FeignException e) {
            log.error("Catalog service unavailable while searching '{}': status={}", q, e.status(), e);
            throw new RuntimeException("Không thể kết nối đến dịch vụ danh mục thuốc. Vui lòng thử lại sau.");
        } catch (Exception e) {
            log.error("Unexpected error while searching '{}': {}", q, e.getMessage(), e);
            throw new RuntimeException("Đã xảy ra lỗi khi tìm kiếm. Vui lòng thử lại sau.");
        }
    }

    @Override
    public PageResponse<Map<String, Object>> lookupDrug(String q, String aToZ, int page, int size) {
        // Reuse catalog search but apply client-side A-Z filter on first letter of name.
        PageResponse<Map<String, Object>> all = search(q, null, page, size, null);
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
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, data.size());
        List<Map<String, Object>> sliced = (fromIndex < data.size())
            ? data.subList(fromIndex, toIndex)
            : List.of();
        return new PageResponse<>(sliced, page, size, total, totalPages);
    }

    private static double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }
}