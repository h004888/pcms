package com.pcms.customerportal.dto.request;

import com.pcms.customerportal.enums.AddressLabel;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Body of PUT /addresses/{id}. All fields optional - partial update
 * semantics: only non-null fields are applied.
 */
public record UpdateAddressRequest(
        AddressLabel label,

        @Size(max = 100)
        String receiverName,

        @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$",
                 message = "phone must be a valid Vietnamese number")
        String phone,

        @Size(max = 100) String province,
        @Size(max = 100) String district,
        @Size(max = 100) String ward,
        @Size(max = 255) String street,

        Boolean isDefault,
        BigDecimal lat,
        BigDecimal lng
) {}
