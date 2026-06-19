package com.pcms.customerportal.controller;

import com.pcms.customerportal.dto.request.CreateAddressRequest;
import com.pcms.customerportal.dto.request.UpdateAddressRequest;
import com.pcms.customerportal.dto.response.AddressResponse;
import com.pcms.customerportal.security.CurrentUser;
import com.pcms.customerportal.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * TICKET-701 - Customer address book (FR14.21).
 * <p>All endpoints require an authenticated customer (X-User-Id header).
 * Ownership is enforced inside the service.
 */
@RestController
@RequestMapping("/addresses")
@Tag(name = "UC14 - Customer Account / Address Book")
public class AddressController {

    private final AddressService service;

    public AddressController(AddressService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List my addresses")
    public ResponseEntity<List<AddressResponse>> list(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId) {
        return ResponseEntity.ok(service.list(CurrentUser.requireCustomerId(userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by id (owner only)")
    public ResponseEntity<AddressResponse> get(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.get(CurrentUser.requireCustomerId(userId), id));
    }

    @PostMapping
    @Operation(summary = "Create a new address")
    public ResponseEntity<AddressResponse> create(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.status(201)
                .body(service.create(CurrentUser.requireCustomerId(userId), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an address (partial update)")
    public ResponseEntity<AddressResponse> update(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(service.update(CurrentUser.requireCustomerId(userId), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete an address")
    public ResponseEntity<Void> delete(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable("id") UUID id) {
        service.softDelete(CurrentUser.requireCustomerId(userId), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Mark this address as default (transactional)")
    public ResponseEntity<AddressResponse> setDefault(
            @RequestHeader(CurrentUser.USER_ID_HEADER) String userId,
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.setDefault(CurrentUser.requireCustomerId(userId), id));
    }
}
