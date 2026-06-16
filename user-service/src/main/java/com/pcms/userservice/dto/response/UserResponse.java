package com.pcms.userservice.dto.response;

import com.pcms.userservice.enums.Role;
import com.pcms.userservice.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String fullName,
    String phone,
    Role role,
    UUID branchId,
    UserStatus status,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
