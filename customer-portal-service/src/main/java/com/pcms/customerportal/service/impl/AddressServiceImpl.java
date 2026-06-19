package com.pcms.customerportal.service.impl;

import com.pcms.common.exception.BusinessException;
import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.dto.request.CreateAddressRequest;
import com.pcms.customerportal.dto.request.UpdateAddressRequest;
import com.pcms.customerportal.dto.response.AddressResponse;
import com.pcms.customerportal.entity.CustomerAddress;
import com.pcms.customerportal.enums.RecordStatus;
import com.pcms.customerportal.repository.CustomerAddressRepository;
import com.pcms.customerportal.service.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Address service implementation.
 * <p>Ownership invariant: every method checks
 * {@code address.customerId.equals(currentCustomerId)} before any
 * mutation. A mismatch raises 403 via {@link InvalidOperationException}
 * to avoid leaking the existence of someone else's address (which a
 * 404 would do).
 */
@Service
public class AddressServiceImpl implements AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressServiceImpl.class);

    private final CustomerAddressRepository repo;

    public AddressServiceImpl(CustomerAddressRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> list(UUID currentCustomerId) {
        return repo.findByCustomerIdAndStatusOrderByIsDefaultDescCreatedAtDesc(
                        currentCustomerId, RecordStatus.ACTIVE)
                .stream()
                .map(AddressResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse get(UUID currentCustomerId, UUID addressId) {
        CustomerAddress a = loadOwned(currentCustomerId, addressId);
        return AddressResponse.from(a);
    }

    @Override
    @Transactional
    public AddressResponse create(UUID currentCustomerId, CreateAddressRequest req) {
        CustomerAddress a = new CustomerAddress();
        a.setCustomerId(currentCustomerId);
        a.setLabel(req.label());
        a.setReceiverName(req.receiverName());
        a.setPhone(req.phone());
        a.setProvince(req.province());
        a.setDistrict(req.district());
        a.setWard(req.ward());
        a.setStreet(req.street());
        a.setIsDefault(Boolean.TRUE.equals(req.isDefault()));
        a.setLat(req.lat());
        a.setLng(req.lng());

        CustomerAddress saved = repo.save(a);

        if (saved.getIsDefault()) {
            // Defer the actual unset to the setDefault call which handles
            // the "exclude self" edge case correctly.
            setDefault(currentCustomerId, saved.getId());
            // Re-load after the transaction commit
            saved = repo.findById(saved.getId()).orElseThrow();
        }

        log.info("[addr] created id={} customer={} default={}",
                saved.getId(), currentCustomerId, saved.getIsDefault());
        return AddressResponse.from(saved);
    }

    @Override
    @Transactional
    public AddressResponse update(UUID currentCustomerId, UUID addressId, UpdateAddressRequest req) {
        CustomerAddress a = loadOwned(currentCustomerId, addressId);
        if (req.label() != null)             a.setLabel(req.label());
        if (req.receiverName() != null)      a.setReceiverName(req.receiverName());
        if (req.phone() != null)             a.setPhone(req.phone());
        if (req.province() != null)          a.setProvince(req.province());
        if (req.district() != null)          a.setDistrict(req.district());
        if (req.ward() != null)              a.setWard(req.ward());
        if (req.street() != null)            a.setStreet(req.street());
        if (req.lat() != null)               a.setLat(req.lat());
        if (req.lng() != null)               a.setLng(req.lng());
        boolean defaultChanged = req.isDefault() != null
                && req.isDefault() != a.getIsDefault();
        if (req.isDefault() != null)          a.setIsDefault(req.isDefault());

        CustomerAddress saved = repo.save(a);

        if (defaultChanged && Boolean.TRUE.equals(saved.getIsDefault())) {
            setDefault(currentCustomerId, saved.getId());
            saved = repo.findById(saved.getId()).orElseThrow();
        }

        log.info("[addr] updated id={} customer={}", addressId, currentCustomerId);
        return AddressResponse.from(saved);
    }

    @Override
    @Transactional
    public void softDelete(UUID currentCustomerId, UUID addressId) {
        CustomerAddress a = loadOwned(currentCustomerId, addressId);
        a.setStatus(RecordStatus.INACTIVE);
        repo.save(a);
        log.info("[addr] soft-deleted id={} customer={}", addressId, currentCustomerId);
    }

    @Override
    @Transactional
    public AddressResponse setDefault(UUID currentCustomerId, UUID addressId) {
        CustomerAddress target = loadOwned(currentCustomerId, addressId);

        // Step 1: clear all other defaults in one UPDATE
        int cleared = repo.clearOtherDefaults(currentCustomerId, target.getId());
        log.debug("[addr] cleared {} other defaults for customer={}", cleared, currentCustomerId);

        // Step 2: set this one as default (idempotent if it already was)
        if (!Boolean.TRUE.equals(target.getIsDefault())) {
            target.setIsDefault(true);
            target = repo.save(target);
        }

        return AddressResponse.from(target);
    }

    // ====================================================================
    // Helpers
    // ====================================================================

    /**
     * Load an address by id and verify ownership. Throws 404 if the
     * address doesn't exist OR if it belongs to a different customer
     * (we do not leak existence to other tenants).
     */
    private CustomerAddress loadOwned(UUID currentCustomerId, UUID addressId) {
        CustomerAddress a = repo.findByIdAndStatus(addressId, RecordStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found: " + addressId,
                        "Không tìm thấy địa chỉ: " + addressId));
        if (!a.getCustomerId().equals(currentCustomerId)) {
            throw new InvalidOperationException(
                    "Address does not belong to current customer",
                    "Địa chỉ không thuộc về khách hàng hiện tại");
        }
        return a;
    }
}
