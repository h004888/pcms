package com.pcms.branchservice.controller;

import com.pcms.branchservice.dto.request.CreateBranchRequest;
import com.pcms.branchservice.dto.request.UpdateBranchRequest;
import com.pcms.branchservice.dto.response.BranchResponse;
import com.pcms.branchservice.dto.response.PageResponse;
import com.pcms.branchservice.dto.response.BranchStaffResponse;
import com.pcms.branchservice.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.List;
import java.util.UUID;

/**
 * UC03 - Manage Branches
 * Authorization: Admin/CEO full, Branch Manager read-only
 */
@RestController
@RequestMapping("/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<BranchResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String district,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("code").ascending());
        return ResponseEntity.ok(PageResponse.from(branchService.list(search, province, district, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(branchService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<BranchResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(branchService.getByCode(code));
    }

    @GetMapping("/{id}/staff")
    public ResponseEntity<List<BranchStaffResponse>> getStaff(@PathVariable UUID id) {
        return ResponseEntity.ok(branchService.getStaff(id));
    }

    @PostMapping
    public ResponseEntity<BranchResponse> create(@Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.create(request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BranchResponse> createMultipart(
            @RequestPart("payload") @Valid CreateBranchRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.create(request, image));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BranchResponse> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateBranchRequest request) {
        return ResponseEntity.ok(branchService.update(id, request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BranchResponse> updateMultipart(@PathVariable UUID id,
            @RequestPart("payload") @Valid UpdateBranchRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(branchService.update(id, request, image));
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable UUID id) {
        return branchService.getImage(id);
    }

    /** AT2: Reassign manager */
    @PutMapping("/{id}/manager")
    public ResponseEntity<BranchResponse> assignManager(@PathVariable UUID id,
            @RequestBody Map<String, UUID> body) {
        return ResponseEntity.ok(branchService.assignManager(id, body.get("managerId")));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(@PathVariable UUID id) {
        branchService.softDelete(id);
    }
}
