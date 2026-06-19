package com.pcms.customerportal.service;

import com.pcms.common.dto.PageResponse;

import java.util.Map;

/**
 * B2C search / lookup aggregator. Delegates text search to catalog-service
 * via Feign (TODO Sprint 8: also call ai-engine-service for semantic search).
 */
public interface ShopSearchService {

    /** SHOP-SEARCH: full-text + (later) AI semantic search */
    PageResponse<Map<String, Object>> search(String q, int page, int size);

    /** SHOP-LOOKUP-DRUG: medicine name search with optional A-Z first-letter filter */
    PageResponse<Map<String, Object>> lookupDrug(String q, String aToZ, int page, int size);

    /** SHOP-LOOKUP-INGREDIENT: search by active ingredient (e.g. Paracetamol) */
    PageResponse<Map<String, Object>> lookupIngredient(String q, int page, int size);

    /** SHOP-LOOKUP-HERB: search traditional herbs (vị thuốc cổ truyền) */
    PageResponse<Map<String, Object>> lookupHerb(String q, int page, int size);
}