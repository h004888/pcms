package com.pcms.userservice.dto.request;

import com.pcms.userservice.enums.Role;
import jakarta.validation.constraints.NotNull;

/** Request payload for UC02 changing user role. */
public record ChangeUserRoleRequest(
        @NotNull(message = "Vai trò không được để trống") Role role) {
}