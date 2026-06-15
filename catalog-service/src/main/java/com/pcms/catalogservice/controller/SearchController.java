package com.pcms.catalogservice.controller;

import com.pcms.catalogservice.entity.Medicine;
import com.pcms.catalogservice.enums.MedicineStatus;
import com.pcms.catalogservice.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC10 - Search Medicines (SCR-SEARCH)
 * Debounced 300ms on client side, autocomplete top 5
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private MedicineRepository medicineRepository;

    /** GET /api/v1/search?q=paracetamol - returns top 5 autocomplete (AT2) */
    @GetMapping
    public List<Medicine> search(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) return List.of();
        return medicineRepository.findTop5ByNameLike(q, PageRequest.of(0, 5));
    }

    /** Full search with filters */
    @GetMapping("/full")
    public List<Medicine> fullSearch(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        return medicineRepository.search(q, categoryId, minPrice, maxPrice, MedicineStatus.ACTIVE, PageRequest.of(0, 100)).getContent();
    }
}
