package com.pcms.catalogservice.service.impl;

import com.pcms.catalogservice.dto.request.CreateMedicineRequest;
import com.pcms.catalogservice.dto.request.UpdateMedicineRequest;
import com.pcms.catalogservice.dto.response.MedicineResponse;
import com.pcms.catalogservice.dto.response.PageResponse;
import com.pcms.catalogservice.entity.Medicine;
import com.pcms.catalogservice.enums.MedicineStatus;
import com.pcms.catalogservice.repository.MedicineRepository;
import com.pcms.catalogservice.service.MedicineService;
import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineServiceImpl(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
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
        if (medicineRepository.existsBySku(request.sku())) {
            throw new DuplicateResourceException("SKU", request.sku());
        }
        Medicine medicine = new Medicine();
        medicine.setSku(request.sku());
        medicine.setName(request.name());
        medicine.setCategoryId(request.categoryId());
        medicine.setSupplierId(request.supplierId());
        medicine.setPrice(request.price());
        medicine.setUnit(request.unit());
        medicine.setPrescriptionRequired(
                request.prescriptionRequired() != null ? request.prescriptionRequired() : Boolean.FALSE
        );
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
        if (request.name() != null) medicine.setName(request.name());
        if (request.price() != null) medicine.setPrice(request.price());
        if (request.unit() != null) medicine.setUnit(request.unit());
        if (request.prescriptionRequired() != null) medicine.setPrescriptionRequired(request.prescriptionRequired());
        if (request.imageUrl() != null) medicine.setImageUrl(request.imageUrl());
        if (request.status() != null) medicine.setStatus(request.status());
        Medicine saved = medicineRepository.save(medicine);
        return MedicineResponse.from(saved);
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
}
