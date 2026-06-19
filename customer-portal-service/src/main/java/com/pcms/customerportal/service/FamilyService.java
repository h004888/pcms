package com.pcms.customerportal.service;

import com.pcms.customerportal.dto.request.CreateFamilyMemberRequest;
import com.pcms.customerportal.dto.request.UpdateFamilyMemberRequest;
import com.pcms.customerportal.dto.response.FamilyMemberResponse;

import java.util.List;
import java.util.UUID;

/**
 * TICKET-702 - Family account service.
 * FR14.22 - Tài khoản gia đình.
 */
public interface FamilyService {

    List<FamilyMemberResponse> list(UUID currentCustomerId);

    FamilyMemberResponse get(UUID currentCustomerId, UUID memberId);

    FamilyMemberResponse create(UUID currentCustomerId, CreateFamilyMemberRequest request);

    FamilyMemberResponse update(UUID currentCustomerId, UUID memberId, UpdateFamilyMemberRequest request);

    void softDelete(UUID currentCustomerId, UUID memberId);
}
