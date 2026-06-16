package com.pcms.customerservice.dto;

import com.pcms.customerservice.entity.Customer;
import com.pcms.customerservice.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for customer.
 */
public record CustomerResponse(
    UUID id,
    String code,
    String name,
    String phone,
    String email,
    String address,
    LocalDate dob,
    Gender gender,
    Integer points,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(
            c.getId(),
            c.getCode(),
            c.getName(),
            c.getPhone(),
            c.getEmail(),
            c.getAddress(),
            c.getDob(),
            c.getGender(),
            c.getPoints(),
            c.getCreatedAt(),
            c.getUpdatedAt()
        );
    }
}
