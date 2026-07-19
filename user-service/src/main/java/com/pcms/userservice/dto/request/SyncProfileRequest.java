package com.pcms.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SyncProfileRequest(
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 20) String phone
) {}
