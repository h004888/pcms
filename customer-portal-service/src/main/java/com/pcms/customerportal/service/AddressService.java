package com.pcms.customerportal.service;

import com.pcms.customerportal.dto.request.CreateAddressRequest;
import com.pcms.customerportal.dto.request.UpdateAddressRequest;
import com.pcms.customerportal.dto.response.AddressResponse;

import java.util.List;
import java.util.UUID;

/**
 * TICKET-701 - Address book service.
 * <p>Authorization: every method operates on the {@code currentCustomerId}
 * (resolved from JWT via {@code X-User-Id}). Ownership is enforced
 * inside the implementation - we never trust a client-supplied id
 * without verifying it belongs to the caller.
 */
public interface AddressService {

    List<AddressResponse> list(UUID currentCustomerId);

    AddressResponse get(UUID currentCustomerId, UUID addressId);

    AddressResponse create(UUID currentCustomerId, CreateAddressRequest request);

    AddressResponse update(UUID currentCustomerId, UUID addressId, UpdateAddressRequest request);

    void softDelete(UUID currentCustomerId, UUID addressId);

    /**
     * Atomically mark the given address as the customer's default.
     * All other addresses for the same customer are first cleared
     * inside the same transaction (FR14.21).
     */
    AddressResponse setDefault(UUID currentCustomerId, UUID addressId);
}
