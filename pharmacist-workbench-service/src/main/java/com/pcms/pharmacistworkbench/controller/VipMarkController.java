package com.pcms.pharmacistworkbench.controller;

import com.pcms.pharmacistworkbench.dto.request.MarkVipRequest;
import com.pcms.pharmacistworkbench.dto.response.VipMarkResponse;
import com.pcms.pharmacistworkbench.service.VipMarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vip-marks")
@Tag(name = "UC16 - VIP Marks (RX-VIP-MARK)")
public class VipMarkController {

    private final VipMarkService service;

    public VipMarkController(VipMarkService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Mark customer as VIP")
    public ResponseEntity<VipMarkResponse> mark(
            @Valid @RequestBody MarkVipRequest request,
            @RequestHeader("X-User-Id") UUID actorId) {
        return ResponseEntity.ok(service.mark(request, actorId));
    }

    @GetMapping("/by-customer/{customerId}")
    @Operation(summary = "Get VIP mark for a customer")
    public ResponseEntity<VipMarkResponse> getByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(service.getByCustomer(customerId));
    }

    @GetMapping("/by-tier/{tier}")
    @Operation(summary = "List VIPs by tier (BRONZE/SILVER/GOLD/PLATINUM)")
    public ResponseEntity<List<VipMarkResponse>> listByTier(@PathVariable String tier) {
        return ResponseEntity.ok(service.listByTier(tier));
    }

    @GetMapping
    @Operation(summary = "List all VIPs (sorted by loyalty score)")
    public ResponseEntity<List<VipMarkResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @DeleteMapping("/{customerId}")
    @Operation(summary = "Remove VIP mark")
    public ResponseEntity<Void> unmark(@PathVariable UUID customerId) {
        service.unmark(customerId);
        return ResponseEntity.noContent().build();
    }
}
