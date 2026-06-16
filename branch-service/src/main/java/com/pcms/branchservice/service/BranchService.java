package com.pcms.branchservice.service;

import com.pcms.branchservice.dto.request.CreateBranchRequest;
import com.pcms.branchservice.dto.request.UpdateBranchRequest;
import com.pcms.branchservice.dto.response.BranchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BranchService {
    Page<BranchResponse> list(String search, Pageable pageable);

    BranchResponse getById(UUID id);

    BranchResponse getByCode(String code);

    BranchResponse create(CreateBranchRequest request);

    BranchResponse update(UUID id, UpdateBranchRequest request);

    BranchResponse assignManager(UUID id, UUID managerId);

    void softDelete(UUID id);
}
