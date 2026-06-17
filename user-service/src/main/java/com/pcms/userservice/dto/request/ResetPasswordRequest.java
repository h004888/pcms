package com.pcms.userservice.dto.request;

import static com.pcms.userservice.dto.request.ValidationMessages.RESET_TOKEN_REQUIRED;

import jakarta.validation.constraints.NotBlank;

/** Request payload for UC01 reset password flow. */
public record ResetPasswordRequest(
                @NotBlank(message = RESET_TOKEN_REQUIRED) String token,

                @ValidResetPassword String newPassword) {
}