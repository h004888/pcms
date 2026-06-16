package com.pcms.userservice.dto.request;

import com.pcms.userservice.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateUserRequest(
    @NotBlank @Email @Size(max = 100) String email,
    @NotBlank @Size(max = 100) String fullName,
    @Size(max = 20) String phone,
    @NotNull Role role,
    UUID branchId
) {}
