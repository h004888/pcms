package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.CustomerFamily;
import com.pcms.customerportal.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FamilyMemberResponse(
        UUID id,
        UUID ownerId,
        String memberName,
        String relationship,
        LocalDate dob,
        Gender gender,
        List<String> allergies,
        List<String> chronicConditions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FamilyMemberResponse from(CustomerFamily f) {
        return new FamilyMemberResponse(
                f.getId(), f.getOwnerId(), f.getMemberName(), f.getRelationship(),
                f.getDob(), f.getGender(),
                JsonStringUtil.toStringList(f.getAllergies()),
                JsonStringUtil.toStringList(f.getChronicConditions()),
                f.getCreatedAt(), f.getUpdatedAt()
        );
    }
}
