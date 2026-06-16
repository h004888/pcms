package com.pcms.supplierservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupplierRequest(
                @NotBlank(message = "Tên nhà cung cấp không được để trống") @Size(max = 150, message = "Tên nhà cung cấp không được vượt quá 150 ký tự") String name,
                @NotBlank(message = "Mã số thuế không được để trống") @Size(max = 20, message = "Mã số thuế không được vượt quá 20 ký tự") String taxCode,
                @Size(max = 100, message = "Người liên hệ không được vượt quá 100 ký tự") String contactPerson,
                @NotBlank(message = "Số điện thoại không được để trống") @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự") String phone,
                @Email(message = "Email không đúng định dạng") @Size(max = 100, message = "Email không được vượt quá 100 ký tự") String email,
                @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự") String address,
                @Size(max = 100, message = "Tên ngân hàng không được vượt quá 100 ký tự") String bankName,
                @Size(max = 30, message = "Số tài khoản không được vượt quá 30 ký tự") String bankAccount) {
}
