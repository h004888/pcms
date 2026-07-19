package com.pcms.customerservice.dto.request;

import com.pcms.customerservice.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerPortalRegisterRequest(
        @NotBlank(message = "Tên khách hàng không được để trống") @Size(max = 100, message = "Tên khách hàng không được vượt quá 100 ký tự") String name,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
        @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$",
                 message = "Số điện thoại phải là số Việt Nam hợp lệ (0xx hoặc +84xx)")
        String phone,

        @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
        @Email(message = "Email không đúng định dạng")
        String email,

        @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự") String address,
        LocalDate dob,
        Gender gender) {
}
