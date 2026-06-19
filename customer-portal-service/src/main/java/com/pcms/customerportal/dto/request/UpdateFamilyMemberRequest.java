package com.pcms.customerportal.dto.request;

import com.pcms.customerportal.enums.Gender;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Body of PUT /family/{id}. All fields optional - partial update.
 */
public record UpdateFamilyMemberRequest(
        @Size(max = 100) String memberName,
        @Size(max = 50)  String relationship,
        LocalDate dob,
        Gender gender,
        List<String> allergies,
        List<String> chronicConditions
) {}
