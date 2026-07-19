package com.pcms.customerservice.dto.request;

import com.pcms.customerservice.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerProvisionRequest(
        @NotNull UUID userId,

        @NotBlank @Size(max = 100) String name,

        @NotBlank @Size(max = 20) String phone,

        @Size(max = 100) String email,

        @Size(max = 255) String address,
        LocalDate dob,
        Gender gender) {
}
