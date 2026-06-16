package com.pcms.catalogservice.controller;

import com.pcms.catalogservice.dto.response.MedicineResponse;
import com.pcms.catalogservice.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * UC10 - Search Medicines (SCR-SEARCH)
 * Debounced 300ms on client side, autocomplete top 5
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /** GET /api/v1/search?q=paracetamol - returns top 5 autocomplete (AT2) */
    @GetMapping
    public List<MedicineResponse> search(@RequestParam(required = false) String q) {
        return searchService.autocomplete(q);
    }

    /** Full search with filters */
    @GetMapping("/full")
    public List<MedicineResponse> fullSearch(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return searchService.fullSearch(q, categoryId, minPrice, maxPrice);
    }
}
