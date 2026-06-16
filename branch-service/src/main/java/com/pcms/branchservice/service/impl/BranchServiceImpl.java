package com.pcms.branchservice.service.impl;

import com.pcms.branchservice.dto.request.CreateBranchRequest;
import com.pcms.branchservice.dto.request.UpdateBranchRequest;
import com.pcms.branchservice.dto.response.BranchResponse;
import com.pcms.branchservice.entity.Branch;
import com.pcms.branchservice.enums.BranchStatus;
import com.pcms.branchservice.repository.BranchRepository;
import com.pcms.branchservice.service.BranchService;
import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository repository;

    public BranchServiceImpl(BranchRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BranchResponse> list(String search, Pageable pageable) {
        return repository.searchBranches(search, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getById(UUID id) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        return toResponse(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getByCode(String code) {
        Branch branch = repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", code));
        return toResponse(branch);
    }

    @Override
    public BranchResponse create(CreateBranchRequest request) {
        if (repository.existsByCode(request.code())) {
            throw new DuplicateResourceException("code", request.code());
        }
        Branch branch = new Branch();
        branch.setCode(request.code());
        branch.setName(request.name());
        branch.setAddress(request.address());
        branch.setPhone(request.phone());
        branch.setStatus(BranchStatus.ACTIVE);
        return toResponse(repository.save(branch));
    }

    @Override
    public BranchResponse update(UUID id, UpdateBranchRequest request) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branch.setName(request.name());
        branch.setAddress(request.address());
        branch.setPhone(request.phone());
        branch.setStatus(request.status());
        return toResponse(repository.save(branch));
    }

    @Override
    public BranchResponse assignManager(UUID id, UUID managerId) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branch.setManagerId(managerId);
        return toResponse(repository.save(branch));
    }

    @Override
    public void softDelete(UUID id) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branch.setStatus(BranchStatus.INACTIVE);
        repository.save(branch);
    }

    private BranchResponse toResponse(Branch entity) {
        return new BranchResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getManagerId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
