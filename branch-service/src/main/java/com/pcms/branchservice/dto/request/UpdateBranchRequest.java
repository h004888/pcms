package com.pcms.branchservice.dto.request;

import com.pcms.branchservice.enums.BranchStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBranchRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 255) String address,
        @NotBlank @Size(max = 20) String phone,
        @NotNull BranchStatus status
) {
}
