package com.pcms.userservice.dto.request;

/**
 * DTO gửi sang customer-service qua Feign khi user đăng ký.
 * Map từ RegisterRequest: fullName -> name, email, phone.
 * Address, dob, gender chưa có khi đăng ký -> null.
 */
public record CustomerRegisterRequest(
    String name,
    String phone,
    String email,
    String address,
    String dob,
    String gender
) {}
