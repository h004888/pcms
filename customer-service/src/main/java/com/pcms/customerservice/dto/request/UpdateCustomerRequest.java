package com.pcms.customerservice.dto.request;

import com.pcms.customerservice.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO for updating an existing customer.
 */
public record UpdateCustomerRequest(
        @NotBlank(message = "Tên khách hàng không được để trống") @Size(max = 100, message = "Tên khách hàng không được vượt quá 100 ký tự") String name,
        @NotBlank(message = "Số điện thoại không được để trống") @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự") String phone,
        @Size(max = 100, message = "Email không được vượt quá 100 ký tự") String email,
        @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự") String address,
        LocalDate dob,
        Gender gender) {
}