package com.pcms.userservice.dto.request;

import com.pcms.userservice.enums.Role;
import com.pcms.userservice.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateUserRequest(
    @NotBlank @Size(max = 100) String fullName,
    @Size(max = 20) String phone,
    @NotNull Role role,
    UUID branchId,
    @NotNull UserStatus status
) {}
