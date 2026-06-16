package com.pcms.catalogservice.service;

import com.pcms.catalogservice.dto.response.MedicineResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SearchService {

    List<MedicineResponse> autocomplete(String q);

    List<MedicineResponse> fullSearch(String q,
                                      UUID categoryId,
                                      BigDecimal minPrice,
                                      BigDecimal maxPrice);
}
