package com.pcms.customerportal.controller;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.client.BranchClient;
import com.pcms.customerportal.dto.response.BranchListResponse;
import com.pcms.customerportal.dto.response.BranchListResponse.BranchSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * B2C Store Locator - lists pharmacy branches for STORE-LOCATOR and
 * STORE-LIST-PROVINCE screens.
 *
 * <p>Maps to SDD:
 * <ul>
 *   <li>GET /store/locator?province=...&page=0  → STORE-LOCATOR + STORE-LIST-PROVINCE</li>
 *   <li>GET /store/locator/{branchId}          → STORE-DETAIL</li>
 * </ul>
 */
@RestController
@RequestMapping("/store")
@Tag(name = "UC14 - Customer Portal / Store Locator")
public class StoreController {

    private static final Logger log = LoggerFactory.getLogger(StoreController.class);

    private final BranchClient branchClient;

    public StoreController(BranchClient branchClient) {
        this.branchClient = branchClient;
    }

    @GetMapping("/locator")
    @Operation(summary = "STORE-LOCATOR - list branches (optionally filtered by province)")
    public ResponseEntity<BranchListResponse> locator(
            @RequestParam(name = "province", required = false) String province,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        List<Map<String, Object>> raw = extractData(branchClient.list(page, size, province, district));

        List<BranchSummary> branches = raw.stream()
                .map(StoreController::toSummary)
                .toList();

        return ResponseEntity.ok(new BranchListResponse(branches, branches.size()));
    }

    @GetMapping("/provinces")
    @Operation(summary = "STORE-PROVINCES - list distinct provinces with branches")
    public ResponseEntity<List<String>> provinces() {
        Map<String, Object> all = branchClient.list(0, 999, null, null);
        List<Map<String, Object>> raw = extractData(all);
        List<String> provinces = raw.stream()
                .map(m -> str(m.get("province")))
                .filter(p -> p != null && !p.isBlank())
                .distinct()
                .sorted()
                .toList();
        return ResponseEntity.ok(provinces);
    }

    @GetMapping("/locator/{branchId}")
    @Operation(summary = "STORE-DETAIL - get single branch by id")
    public ResponseEntity<BranchSummary> detail(@PathVariable("branchId") String branchId) {
        Map<String, Object> raw = branchClient.getById(branchId);
        if (raw == null || raw.isEmpty()) {
            throw new ResourceNotFoundException("MSG31", "Branch not found: " + branchId);
        }
        return ResponseEntity.ok(toSummary(raw));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> extractData(Map<String, Object> pageResponse) {
        Object data = pageResponse.get("data");
        if (data instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    private static BranchSummary toSummary(Map<String, Object> m) {
        return new BranchSummary(
                str(m.get("id")),
                str(m.get("code")),
                str(m.get("name")),
                str(m.get("address")),
                str(m.get("phone")),
                str(m.get("province")),
                str(m.get("district")),
                toDouble(m.get("lat")),
                toDouble(m.get("lng")),
                str(m.getOrDefault("openHours", "06:00 - 23:00")),
                List.of()
        );
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return null; }
    }
}