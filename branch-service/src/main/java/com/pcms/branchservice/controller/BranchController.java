package com.pcms.branchservice.controller;

import com.pcms.branchservice.entity.Branch;
import com.pcms.branchservice.enums.BranchStatus;
import com.pcms.branchservice.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * UC03 - Manage Branches
 * Authorization: Admin/CEO full, Branch Manager read-only
 */
@RestController
@RequestMapping("/branches")
public class BranchController {

    @Autowired
    private BranchRepository branchRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("code").ascending());
        Page<Branch> branches = branchRepository.searchBranches(search, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", branches.getContent());
        response.put("page", branches.getNumber());
        response.put("size", branches.getSize());
        response.put("total", branches.getTotalElements());
        response.put("totalPages", branches.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Branch> getById(@PathVariable UUID id) {
        return branchRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Branch> getByCode(@PathVariable String code) {
        return branchRepository.findByCode(code)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Branch branch) {
        if (branchRepository.existsByCode(branch.getCode())) {
            return ResponseEntity.badRequest().body(Map.of("code", "MSG09", "message", "Branch code already exists"));
        }
        if (branch.getStatus() == null) branch.setStatus(BranchStatus.ACTIVE);
        Branch saved = branchRepository.save(branch);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Branch> update(@PathVariable UUID id, @RequestBody Branch details) {
        Optional<Branch> optional = branchRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Branch branch = optional.get();
        branch.setName(details.getName());
        branch.setAddress(details.getAddress());
        branch.setPhone(details.getPhone());
        branch.setStatus(details.getStatus());
        return ResponseEntity.ok(branchRepository.save(branch));
    }

    /** AT2: Reassign manager */
    @PutMapping("/{id}/manager")
    public ResponseEntity<?> assignManager(@PathVariable UUID id, @RequestBody Map<String, UUID> body) {
        Optional<Branch> optional = branchRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Branch branch = optional.get();
        branch.setManagerId(body.get("managerId"));
        return ResponseEntity.ok(branchRepository.save(branch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@PathVariable UUID id) {
        Optional<Branch> optional = branchRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();
        Branch branch = optional.get();
        branch.setStatus(BranchStatus.INACTIVE);
        branchRepository.save(branch);
        return ResponseEntity.ok(Map.of("code", "MSG08", "message", "Branch deactivated"));
    }
}
