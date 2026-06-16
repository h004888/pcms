package com.pcms.supplierservice.service.impl;

import com.pcms.supplierservice.dto.request.CreateSupplierRequest;
import com.pcms.supplierservice.dto.request.UpdateSupplierRequest;
import com.pcms.supplierservice.dto.response.SupplierResponse;
import com.pcms.supplierservice.entity.Supplier;
import com.pcms.supplierservice.enums.SupplierStatus;
import com.pcms.supplierservice.repository.SupplierRepository;
import com.pcms.supplierservice.service.SupplierService;
import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;

    public SupplierServiceImpl(SupplierRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> list(String search, Pageable pageable) {
        return repository.search(search, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getById(UUID id) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        return toResponse(supplier);
    }

    @Override
    public SupplierResponse create(CreateSupplierRequest request) {
        if (repository.existsByTaxCode(request.taxCode())) {
            throw new DuplicateResourceException("taxCode", request.taxCode());
        }
        Supplier supplier = new Supplier();
        supplier.setName(request.name());
        supplier.setTaxCode(request.taxCode());
        supplier.setContactPerson(request.contactPerson());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
        supplier.setAddress(request.address());
        supplier.setBankName(request.bankName());
        supplier.setBankAccount(request.bankAccount());
        supplier.setStatus(SupplierStatus.ACTIVE);
        return toResponse(repository.save(supplier));
    }

    @Override
    public SupplierResponse update(UUID id, UpdateSupplierRequest request) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        supplier.setName(request.name());
        supplier.setContactPerson(request.contactPerson());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
        supplier.setAddress(request.address());
        supplier.setBankName(request.bankName());
        supplier.setBankAccount(request.bankAccount());
        supplier.setStatus(request.status());
        return toResponse(repository.save(supplier));
    }

    @Override
    public void softDelete(UUID id) {
        Supplier supplier = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        supplier.setStatus(SupplierStatus.INACTIVE);
        repository.save(supplier);
    }

    private SupplierResponse toResponse(Supplier entity) {
        return new SupplierResponse(
                entity.getId(),
                entity.getName(),
                entity.getTaxCode(),
                entity.getContactPerson(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getBankName(),
                entity.getBankAccount(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
