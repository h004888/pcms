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

    /** Backward-compatible endpoint: GET /api/v1/search?q=paracetamol */
    @GetMapping
    public List<MedicineResponse> search(@RequestParam(required = false) String q) {
        return searchService.autocomplete(q);
    }

    /** Spec endpoint: GET /api/v1/search/medicines/autocomplete?q=para */
    @GetMapping("/medicines/autocomplete")
    public List<MedicineResponse> autocomplete(@RequestParam(required = false) String q) {
        return searchService.autocomplete(q);
    }

    /**
     * Spec endpoint: GET
     * /api/v1/search/medicines?q=&categoryId=&minPrice=&maxPrice=&inStock=
     */
    @GetMapping("/medicines")
    public List<MedicineResponse> searchMedicines(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false, name = "category") UUID category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock) {
        return searchService.fullSearch(q, categoryId != null ? categoryId : category, minPrice, maxPrice, inStock);
    }

    /** Backward-compatible full search with filters */
    @GetMapping("/full")
    public List<MedicineResponse> fullSearch(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false, name = "category") UUID category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock) {
        return searchService.fullSearch(q, categoryId != null ? categoryId : category, minPrice, maxPrice, inStock);
    }
}
