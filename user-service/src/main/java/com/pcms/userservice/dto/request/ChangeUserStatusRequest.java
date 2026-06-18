package com.pcms.userservice.dto.request;

import com.pcms.userservice.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

/** Request payload for UC02 changing user status. */
public record ChangeUserStatusRequest(
        @NotNull(message = "Trạng thái không được để trống") UserStatus status) {
}