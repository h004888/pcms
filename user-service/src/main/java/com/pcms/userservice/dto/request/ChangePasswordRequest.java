package com.pcms.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for {@code PUT /api/v1/auth/password} (TICKET-102, FR1.3).
 *
 * <p>FR1.3 mandates: minimum 8 characters, must contain uppercase, number, and
 * special character. The pattern is enforced here so the validation error
 * surfaces with MSG33 before reaching the service layer.
 */
public record ChangePasswordRequest(
        @NotBlank(message = "Mật khẩu hiện tại không được để trống")
        String currentPassword,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 8, max = 64, message = "Mật khẩu phải có độ dài từ 8 đến 64 ký tự")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/~`|]).+$",
                message = "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 số và 1 ký tự đặc biệt"
        )
        String newPassword,

        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        String confirmPassword
) {
}
