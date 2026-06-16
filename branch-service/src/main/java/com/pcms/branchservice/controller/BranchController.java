package com.pcms.branchservice.controller;

import com.pcms.branchservice.dto.request.CreateBranchRequest;
import com.pcms.branchservice.dto.request.UpdateBranchRequest;
import com.pcms.branchservice.dto.response.BranchResponse;
import com.pcms.branchservice.dto.response.PageResponse;
import com.pcms.branchservice.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("code").ascending());
        return ResponseEntity.ok(PageResponse.from(branchService.list(search, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(branchService.getById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<BranchResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(branchService.getByCode(code));
    }

    @PostMapping
    public ResponseEntity<BranchResponse> create(@Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.ok(branchService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BranchResponse> update(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateBranchRequest request) {
        return ResponseEntity.ok(branchService.update(id, request));
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
