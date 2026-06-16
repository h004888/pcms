package com.pcms.customerservice.service;

import com.pcms.customerservice.dto.CreateCustomerRequest;
import com.pcms.customerservice.dto.CustomerResponse;
import com.pcms.customerservice.dto.UpdateCustomerRequest;

import java.util.UUID;

/**
 * Service interface for Customer (UC08).
 */
public interface CustomerService {

    /** Paginated list with optional search by name/phone/code. */
    org.springframework.data.domain.Page<CustomerResponse> list(String search, int page, int size);

    CustomerResponse getById(UUID id);

    CustomerResponse getByCode(String code);

    /** Auto-generate CUST-yyyy#### and persist. */
    CustomerResponse create(CreateCustomerRequest request);

    CustomerResponse update(UUID id, UpdateCustomerRequest request);

    void softDelete(UUID id);

    /**
     * BR07: Award loyalty points.
     * <p>If {@code refOrderId} is provided, the operation is idempotent —
     * subsequent calls with the same orderId will be no-ops.
     *
     * @param id         customer id
     * @param points     points to add (can be negative)
     * @param refOrderId optional order reference for idempotency
     * @param reason     human-readable reason (audit log)
     */
    CustomerResponse addPoints(UUID id, int points, UUID refOrderId, String reason);
}
