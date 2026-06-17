package com.pcms.customerservice.dto;

import com.pcms.customerservice.entity.Customer;
import com.pcms.customerservice.enums.CustomerStatus;
import com.pcms.customerservice.enums.Gender;
import com.pcms.customerservice.enums.LoyaltyTier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
    LoyaltyTier tier,
    CustomerStatus status,
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
            c.getTier() != null ? c.getTier() : LoyaltyTier.BRONZE,
            c.getStatus(),
            c.getCreatedAt(),
            c.getUpdatedAt()
        );
    }
}
