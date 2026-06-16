package com.pcms.supplierservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupplierRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 20) String taxCode,
        @Size(max = 100) String contactPerson,
        @NotBlank @Size(max = 20) String phone,
        @Email @Size(max = 100) String email,
        @Size(max = 255) String address,
        @Size(max = 100) String bankName,
        @Size(max = 30) String bankAccount
) {
}
