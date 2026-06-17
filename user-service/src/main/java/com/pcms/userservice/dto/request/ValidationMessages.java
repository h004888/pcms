package com.pcms.userservice.dto.request;

/** Shared Vietnamese validation messages for user-service request DTOs. */
final class ValidationMessages {

    static final String EMAIL_REQUIRED = "Email không được để trống";
    static final String EMAIL_INVALID = "Email không đúng định dạng";
    static final String EMAIL_TOO_LONG = "Email không được vượt quá 100 ký tự";
    static final String RESET_TOKEN_REQUIRED = "Token đặt lại mật khẩu không được để trống";
    static final String NEW_PASSWORD_REQUIRED = "Mật khẩu mới không được để trống";
    static final String NEW_PASSWORD_SIZE = "Mật khẩu mới phải từ 8 đến 72 ký tự";

    private ValidationMessages() {
    }
}