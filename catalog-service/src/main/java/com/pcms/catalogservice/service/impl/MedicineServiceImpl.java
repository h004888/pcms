package com.pcms.catalogservice.service.impl;

import com.pcms.catalogservice.client.CategoryClient;
import com.pcms.catalogservice.client.SupplierClient;
import com.pcms.catalogservice.dto.request.CreateMedicineRequest;
import com.pcms.catalogservice.dto.request.UpdateMedicineRequest;
import com.pcms.catalogservice.dto.response.MedicineResponse;
import com.pcms.catalogservice.dto.response.PageResponse;
import com.pcms.catalogservice.entity.Medicine;
import com.pcms.catalogservice.enums.MedicineStatus;
import com.pcms.catalogservice.repository.MedicineRepository;
import com.pcms.catalogservice.service.ImageStorageService;
import com.pcms.catalogservice.service.MedicineService;
import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import feign.FeignException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final CategoryClient categoryClient;
    private final SupplierClient supplierClient;
    private final ImageStorageService imageStorageService;

    public MedicineServiceImpl(MedicineRepository medicineRepository,
            CategoryClient categoryClient,
            SupplierClient supplierClient,
            ImageStorageService imageStorageService) {
        this.medicineRepository = medicineRepository;
        this.categoryClient = categoryClient;
        this.supplierClient = supplierClient;
        this.imageStorageService = imageStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MedicineResponse> list(String search,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size) {
        return search(search, categoryId, minPrice, maxPrice, MedicineStatus.ACTIVE, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponse getById(UUID id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", id));
        return MedicineResponse.from(medicine);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponse getBySku(String sku) {
        Medicine medicine = medicineRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", sku));
        return MedicineResponse.from(medicine);
    }

    @Override
    @Transactional
    public MedicineResponse create(CreateMedicineRequest request) {
        validateCategory(request.categoryId());
        validateSupplier(request.supplierId());
        String sku = normalizeSku(request.sku());
        if (medicineRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("SKU", sku);
        }
        Medicine medicine = new Medicine();
        medicine.setSku(sku);
        medicine.setName(request.name());
        medicine.setCategoryId(request.categoryId());
        medicine.setSupplierId(request.supplierId());
        medicine.setPrice(request.price());
        medicine.setUnit(request.unit());
        medicine.setPrescriptionRequired(
                request.prescriptionRequired() != null ? request.prescriptionRequired() : Boolean.FALSE);
        medicine.setImageUrl(request.imageUrl());
        medicine.setStatus(MedicineStatus.ACTIVE);
        Medicine saved = medicineRepository.save(medicine);
        return MedicineResponse.from(saved);
    }

    @Override
    @Transactional
    public MedicineResponse update(UUID id, UpdateMedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", id));
        if (request.name() != null)
            medicine.setName(request.name());
        if (request.categoryId() != null) {
            validateCategory(request.categoryId());
            medicine.setCategoryId(request.categoryId());
        }
        if (request.supplierId() != null) {
            validateSupplier(request.supplierId());
            medicine.setSupplierId(request.supplierId());
        }
        if (request.price() != null)
            medicine.setPrice(request.price());
        if (request.unit() != null)
            medicine.setUnit(request.unit());
        if (request.prescriptionRequired() != null)
            medicine.setPrescriptionRequired(request.prescriptionRequired());
        if (request.imageUrl() != null)
            medicine.setImageUrl(request.imageUrl());
        if (request.status() != null)
            medicine.setStatus(request.status());
        Medicine saved = medicineRepository.save(medicine);
        return MedicineResponse.from(saved);
    }

    @Override
    @Transactional
    public MedicineResponse updateImage(UUID id, MultipartFile image) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", id));
        String imageUrl = imageStorageService.store(id, image);
        medicine.setImageUrl(imageUrl);
        return MedicineResponse.from(medicineRepository.save(medicine));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", id));
        medicine.setStatus(MedicineStatus.INACTIVE);
        medicineRepository.save(medicine);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCategoryId(UUID categoryId) {
        return medicineRepository.countByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MedicineResponse> search(String search,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            MedicineStatus status,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("name").ascending());
        Page<Medicine> result = medicineRepository.search(search, categoryId, minPrice, maxPrice,
                status != null ? status : MedicineStatus.ACTIVE, pageable);
        return PageResponse.from(result.map(MedicineResponse::from));
    }

    private String normalizeSku(String requestedSku) {
        if (requestedSku != null && !requestedSku.isBlank()) {
            return requestedSku.trim();
        }
        long next = medicineRepository.count() + 1;
        String sku;
        do {
            sku = "MED-%06d".formatted(next++);
        } while (medicineRepository.existsBySku(sku));
        return sku;
    }

    private void validateCategory(UUID categoryId) {
        try {
            categoryClient.getById(categoryId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Category", categoryId);
        } catch (FeignException ex) {
            throw new InvalidOperationException("Cannot validate category", "Không thể kiểm tra danh mục thuốc");
        }
    }

    private void validateSupplier(UUID supplierId) {
        if (supplierId == null) {
            return;
        }
        try {
            supplierClient.getById(supplierId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Supplier", supplierId);
        } catch (FeignException ex) {
            throw new InvalidOperationException("Cannot validate supplier", "Không thể kiểm tra nhà cung cấp");
        }
    }
}
