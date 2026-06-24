package com.pcms.catalogservice.service;

import com.pcms.catalogservice.dto.request.CreateMedicineRequest;
import com.pcms.catalogservice.dto.request.UpdateMedicineRequest;
import com.pcms.catalogservice.dto.response.MedicineResponse;
import com.pcms.catalogservice.dto.response.PageResponse;
import com.pcms.catalogservice.enums.MedicineStatus;
import org.springframework.web.multipart.MultipartFile;

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

        MedicineResponse create(CreateMedicineRequest request, MultipartFile image);

        MedicineResponse update(UUID id, UpdateMedicineRequest request);

        MedicineResponse update(UUID id, UpdateMedicineRequest request, MultipartFile image);

        MedicineResponse updateImage(UUID id, MultipartFile image, String imageUrl);

        void softDelete(UUID id);

        long countByCategoryId(UUID categoryId);

        PageResponse<MedicineResponse> search(String search,
                        UUID categoryId,
                        BigDecimal minPrice,
                        BigDecimal maxPrice,
                        MedicineStatus status,
                        int page,
                        int size);
}
