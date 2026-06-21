package com.pcms.branchservice.dto.request;

import com.pcms.branchservice.enums.BranchStatus;
import jakarta.validation.constraints.Size;

/**
 * Partial update DTO for branches. All fields are nullable - only non-null
 * fields will be updated. This allows clients to PATCH a single field
 * (e.g. just the name) without sending the entire branch.
 */
public record UpdateBranchRequest(
        @Size(max = 100) String name,
        @Size(max = 255) String address,
        @Size(max = 20) String phone,
        BranchStatus status
) {
}