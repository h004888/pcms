package com.pcms.branchservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(
        @NotBlank @Size(max = 10) String code,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 255) String address,
        @NotBlank @Size(max = 20) String phone
) {
}
