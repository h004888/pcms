package com.pcms.customerservice.service;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerservice.dto.request.CreateCustomerRequest;
import com.pcms.customerservice.dto.request.CustomerPortalRegisterRequest;
import com.pcms.customerservice.dto.request.UpdateCustomerRequest;
import com.pcms.customerservice.dto.response.CustomerHistoryResponse;
import com.pcms.customerservice.dto.response.CustomerOrderSummaryResponse;
import com.pcms.customerservice.dto.response.CustomerResponse;
import com.pcms.customerservice.dto.response.LoyaltyTransactionResponse;

import java.util.UUID;

/**
 * Service interface for Customer (UC08).
 */
public interface CustomerService {

    /** Paginated list with optional search by name/phone/code. */
    org.springframework.data.domain.Page<CustomerResponse> list(String search, int page, int size);

    CustomerResponse getById(UUID id);

    CustomerResponse getByCode(String code);

    CustomerResponse getByPhone(String phone);

    CustomerResponse getByEmail(String email);

    String getTier(UUID id);

    PageResponse<CustomerOrderSummaryResponse> getOrders(UUID id, int page, int size);

    PageResponse<LoyaltyTransactionResponse> getPoints(UUID id, int page, int size);

    CustomerHistoryResponse getHistory(UUID id, int page, int size);

    /** Auto-generate CUST-yyyy#### and persist. */
    CustomerResponse create(CreateCustomerRequest request);

    CustomerResponse update(UUID id, UpdateCustomerRequest request);

    CustomerResponse register(CustomerPortalRegisterRequest request);

    CustomerResponse updatePortalProfile(UUID id, CustomerPortalRegisterRequest request);

    CustomerResponse updatePortalProfileByEmail(String email, CustomerPortalRegisterRequest request);

    void softDelete(UUID id);

    /**
     * BR07: Award loyalty points.
     * <p>
     * If {@code refOrderId} is provided, the operation is idempotent —
     * subsequent calls with the same orderId will be no-ops.
     *
     * @param id         customer id
     * @param points     points to add (can be negative)
     * @param refOrderId optional order reference for idempotency
     * @param reason     human-readable reason (audit log)
     */
    CustomerResponse addPoints(UUID id, int points, UUID refOrderId, String reason);
}
