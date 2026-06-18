package com.pcms.userservice.dto.request;

/** Request payload for UC01 forgot password flow. */
public record ForgotPasswordRequest(
                @ValidUserEmail String email) {
}