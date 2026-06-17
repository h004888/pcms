package com.pcms.customerservice.dto.response;

import com.pcms.customerservice.entity.Customer;
import com.pcms.customerservice.enums.CustomerStatus;
import com.pcms.customerservice.enums.Gender;
import com.pcms.customerservice.enums.LoyaltyTier;

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
        CustomerStatus status,
        Integer points,
        LoyaltyTier tier,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCode(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getDob(),
                customer.getGender(),
                customer.getStatus(),
                customer.getPoints(),
                customer.getTier(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}