package com.pcms.catalogservice.service;

import com.pcms.catalogservice.dto.request.CreateMedicineRequest;
import com.pcms.catalogservice.dto.request.UpdateMedicineRequest;
import com.pcms.catalogservice.dto.response.MedicineResponse;
import com.pcms.catalogservice.dto.response.PageResponse;
import com.pcms.catalogservice.enums.MedicineStatus;

import java.math.BigDecimal;
import java.util.UUID;

public interface MedicineService {

    PageResponse<MedicineResponse> list(String search,
                                        UUID categoryId,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        int page,
                                        int size);

    MedicineResponse getById(UUID id);

    MedicineResponse getBySku(String sku);

    MedicineResponse create(CreateMedicineRequest request);

    MedicineResponse update(UUID id, UpdateMedicineRequest request);

    void softDelete(UUID id);

    PageResponse<MedicineResponse> search(String search,
                                          UUID categoryId,
                                          BigDecimal minPrice,
                                          BigDecimal maxPrice,
                                          MedicineStatus status,
                                          int page,
                                          int size);
}
