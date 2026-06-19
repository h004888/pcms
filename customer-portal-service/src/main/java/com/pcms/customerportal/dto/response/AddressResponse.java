package com.pcms.customerportal.dto.response;

import com.pcms.customerportal.entity.CustomerAddress;
import com.pcms.customerportal.enums.AddressLabel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for address APIs.
 */
public record AddressResponse(
        UUID id,
        UUID customerId,
        AddressLabel label,
        String receiverName,
        String phone,
        String province,
        String district,
        String ward,
        String street,
        Boolean isDefault,
        BigDecimal lat,
        BigDecimal lng,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AddressResponse from(CustomerAddress a) {
        return new AddressResponse(
                a.getId(), a.getCustomerId(), a.getLabel(),
                a.getReceiverName(), a.getPhone(),
                a.getProvince(), a.getDistrict(), a.getWard(), a.getStreet(),
                a.getIsDefault(), a.getLat(), a.getLng(),
                a.getCreatedAt(), a.getUpdatedAt()
        );
    }
}
