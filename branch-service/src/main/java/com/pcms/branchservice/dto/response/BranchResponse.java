package com.pcms.branchservice.dto.response;

import com.pcms.branchservice.enums.BranchStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record BranchResponse(
        UUID id,
        String code,
        String name,
        String address,
        String phone,
        String province,
        String district,
        Double lat,
        Double lng,
        String openHours,
        UUID managerId,
        BranchStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
