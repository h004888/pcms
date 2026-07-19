package com.pcms.userservice.dto.request;

import java.util.UUID;

/**
 * DTO gửi sang customer-service qua Feign khi user đăng ký.
 * Map từ RegisterRequest: fullName -> name, email, phone.
 * Address, dob, gender chưa có khi đăng ký -> null.
 */
public record CustomerRegisterRequest(
    UUID userId,
    String name,
    String phone,
    String email,
    String address,
    String dob,
    String gender
) {}
