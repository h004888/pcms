package com.pcms.branchservice.service.impl;

import com.pcms.branchservice.client.UserClient;
import com.pcms.branchservice.dto.request.CreateBranchRequest;
import com.pcms.branchservice.dto.request.UpdateBranchRequest;
import com.pcms.branchservice.dto.response.BranchResponse;
import com.pcms.branchservice.dto.response.BranchStaffResponse;
import com.pcms.branchservice.entity.Branch;
import com.pcms.branchservice.enums.BranchStatus;
import com.pcms.branchservice.repository.BranchRepository;
import com.pcms.branchservice.service.BranchService;
import com.pcms.common.exception.DuplicateResourceException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Map;

@Service
@Transactional
public class BranchServiceImpl implements BranchService {

    private final BranchRepository repository;
    private final UserClient userClient;

    public BranchServiceImpl(BranchRepository repository, UserClient userClient) {
        this.repository = repository;
        this.userClient = userClient;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BranchResponse> list(String search, String province, String district, Pageable pageable) {
        return repository.searchBranches(search, province, district, pageable).map(this::toResponse);
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
        if (request.province() != null) branch.setProvince(request.province());
        if (request.district() != null) branch.setDistrict(request.district());
        if (request.lat() != null) branch.setLat(request.lat());
        if (request.lng() != null) branch.setLng(request.lng());
        if (request.openHours() != null) branch.setOpenHours(request.openHours());
        return toResponse(repository.save(branch));
    }

    @Override
    public BranchResponse create(CreateBranchRequest request, MultipartFile image) {
        BranchResponse created = create(request);
        return image == null || image.isEmpty() ? created : storeImage(created.id(), image);
    }

    @Override
    public BranchResponse update(UUID id, UpdateBranchRequest request) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        if (branch.getStatus() == BranchStatus.INACTIVE
                && (request.status() != BranchStatus.ACTIVE
                        || request.name() != null
                        || request.address() != null
                        || request.phone() != null)) {
            throw inactiveBranchOperation();
        }
        if (request.name() != null) branch.setName(request.name());
        if (request.address() != null) branch.setAddress(request.address());
        if (request.phone() != null) branch.setPhone(request.phone());
        if (request.status() != null) branch.setStatus(request.status());
        if (request.province() != null) branch.setProvince(request.province());
        if (request.district() != null) branch.setDistrict(request.district());
        if (request.lat() != null) branch.setLat(request.lat());
        if (request.lng() != null) branch.setLng(request.lng());
        if (request.openHours() != null) branch.setOpenHours(request.openHours());
        return toResponse(repository.save(branch));
    }

    @Override
    public BranchResponse update(UUID id, UpdateBranchRequest request, MultipartFile image) {
        BranchResponse updated = update(id, request);
        return image == null || image.isEmpty() ? updated : storeImage(id, image);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getImage(UUID id) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        if (branch.getImageData() == null || branch.getImageData().length == 0) {
            throw new ResourceNotFoundException("Branch image", id);
        }
        MediaType contentType = branch.getImageContentType() == null
                ? MediaType.IMAGE_JPEG
                : MediaType.parseMediaType(branch.getImageContentType());
        return ResponseEntity.ok().contentType(contentType).body(branch.getImageData());
    }

    @Override
    public BranchResponse assignManager(UUID id, UUID managerId) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        requireActive(branch);
        validateManager(managerId);
        ensureManagerNotAssignedToAnotherActiveBranch(id, managerId);
        branch.setManagerId(managerId);
        return toResponse(repository.save(branch));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchStaffResponse> getStaff(UUID id) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        Map<String, Object> response = userClient.listUsers(null, null, "ACTIVE", branch.getId(), 0, 100);
        Object data = response.get("data");
        if (!(data instanceof List<?> rows)) {
            return List.of();
        }
        List<BranchStaffResponse> staff = new ArrayList<>();
        for (Object row : rows) {
            if (row instanceof Map<?, ?> user) {
                staff.add(mapStaff(user));
            }
        }
        return staff;
    }

    private void validateManager(UUID managerId) {
        if (managerId == null) {
            throw new InvalidOperationException("Manager ID is required", "Mã quản lý chi nhánh là bắt buộc");
        }
        try {
            Map<String, Object> user = userClient.getUserById(managerId);
            if (user == null || "UNREACHABLE".equals(user.get("status"))) {
                throw new ResourceNotFoundException("User", managerId);
            }
            if (!"BRANCH_MANAGER".equalsIgnoreCase(String.valueOf(user.get("role")))) {
                throw new InvalidOperationException(
                        "Assigned manager must have BRANCH_MANAGER role",
                        "Người quản lý được gán phải có vai trò BRANCH_MANAGER");
            }
            if (!"ACTIVE".equalsIgnoreCase(String.valueOf(user.get("status")))) {
                throw new InvalidOperationException(
                        "Assigned manager must be active",
                        "Người quản lý được gán phải đang hoạt động");
            }
        } catch (feign.FeignException.NotFound ex) {
            throw new ResourceNotFoundException("User", managerId);
        }
    }

    private void ensureManagerNotAssignedToAnotherActiveBranch(UUID branchId, UUID managerId) {
        List<Branch> assignedBranches = repository.findByManagerId(managerId);
        boolean assignedElsewhere = assignedBranches.stream()
                .anyMatch(branch -> !branch.getId().equals(branchId) && branch.getStatus() == BranchStatus.ACTIVE);
        if (assignedElsewhere) {
            throw new InvalidOperationException(
                    "Manager is already assigned to another active branch",
                    "Quản lý này đã được gán cho một chi nhánh đang hoạt động khác");
        }
    }

    @Override
    public void softDelete(UUID id) {
        Branch branch = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branch.setStatus(BranchStatus.INACTIVE);
        repository.save(branch);
    }

    public BranchResponse toResponse(Branch entity) {
        return new BranchResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getProvince(),
                entity.getDistrict(),
                entity.getLat(),
                entity.getLng(),
                entity.getOpenHours(),
                entity.getImageContentType() == null ? null : "/branches/" + entity.getId() + "/image",
                entity.getManagerId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private BranchResponse storeImage(UUID id, MultipartFile image) {
        if (image.getSize() > 2L * 1024L * 1024L) {
            throw new InvalidOperationException("Branch image must be <= 2MB", "Ảnh chi nhánh không được vượt quá 2MB");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidOperationException("Unsupported branch image type", "Vui lòng chọn tệp ảnh hợp lệ");
        }
        try {
            Branch branch = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
            requireActive(branch);
            branch.setImageData(image.getBytes());
            branch.setImageContentType(contentType);
            return toResponse(repository.save(branch));
        } catch (IOException ex) {
            throw new InvalidOperationException("Cannot read branch image", "Không thể đọc ảnh chi nhánh");
        }
    }

    private void requireActive(Branch branch) {
        if (branch.getStatus() != BranchStatus.ACTIVE) {
            throw inactiveBranchOperation();
        }
    }

    private InvalidOperationException inactiveBranchOperation() {
        return new InvalidOperationException(
                "Cannot perform operations for an inactive branch",
                "Chi nhánh đã ngừng hoạt động. Vui lòng kích hoạt lại trước khi thực hiện thao tác này.");
    }

    private BranchStaffResponse mapStaff(Map<?, ?> user) {
        return new BranchStaffResponse(
                uuidValue(user.get("id")),
                stringValue(user.get("email")),
                stringValue(user.get("fullName")),
                stringValue(user.get("phone")),
                stringValue(user.get("role")),
                stringValue(user.get("status")));
    }

    private UUID uuidValue(Object value) {
        return value == null ? null : UUID.fromString(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
