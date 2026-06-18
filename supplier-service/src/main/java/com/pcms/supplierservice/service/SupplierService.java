package com.pcms.supplierservice.service;

import com.pcms.supplierservice.dto.request.CreateSupplierRequest;
import com.pcms.supplierservice.dto.request.UpdateSupplierRequest;
import com.pcms.supplierservice.dto.response.SupplierHistoryResponse;
import com.pcms.supplierservice.dto.response.SupplierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SupplierService {
    Page<SupplierResponse> list(String search, Pageable pageable);

    SupplierResponse getById(UUID id);

    SupplierResponse create(CreateSupplierRequest request);

    SupplierResponse update(UUID id, UpdateSupplierRequest request);

    List<SupplierHistoryResponse> history(UUID id);

    void softDelete(UUID id);
}
