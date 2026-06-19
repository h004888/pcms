package com.pcms.customerportal.dto.request;

import com.pcms.customerportal.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Body of POST /family.
 * FR14.22 - Tài khoản gia đình.
 * allergies and chronicConditions are arrays of free-text or medicine-id strings.
 */
public record CreateFamilyMemberRequest(
        @NotBlank(message = "memberName is required")
        @Size(max = 100)
        String memberName,

        @NotBlank(message = "relationship is required")
        @Size(max = 50)
        String relationship,

        LocalDate dob,
        Gender gender,
        List<String> allergies,
        List<String> chronicConditions
) {}
