package com.pcms.customerportal.dto.request;

import com.pcms.customerportal.enums.AddressLabel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Body of POST /addresses.
 * FR14.21 - Customer delivery address book.
 */
public record CreateAddressRequest(
        @NotBlank(message = "label is required")
        AddressLabel label,

        @NotBlank(message = "receiverName is required")
        @Size(max = 100, message = "receiverName must be <= 100 chars")
        String receiverName,

        @NotBlank(message = "phone is required")
        @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$",
                 message = "phone must be a valid Vietnamese number")
        String phone,

        @NotBlank(message = "province is required")
        @Size(max = 100)
        String province,

        @NotBlank(message = "district is required")
        @Size(max = 100)
        String district,

        @NotBlank(message = "ward is required")
        @Size(max = 100)
        String ward,

        @NotBlank(message = "street is required")
        @Size(max = 255)
        String street,

        Boolean isDefault,

        BigDecimal lat,
        BigDecimal lng
) {
    public Boolean isDefaultOrFalse() {
        return isDefault != null && isDefault;
    }
}
