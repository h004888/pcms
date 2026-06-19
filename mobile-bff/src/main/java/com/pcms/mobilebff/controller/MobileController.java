package com.pcms.mobilebff.controller;

import com.pcms.mobilebff.dto.response.MobileHomeResponse;
import com.pcms.mobilebff.service.MobileHomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/mobile")
@Tag(name = "UC17 - Mobile BFF (MOBILE-HOME)")
public class MobileController {

    private final MobileHomeService homeService;

    public MobileController(MobileHomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/home")
    @Operation(summary = "Get mobile home page (aggregated from notification/order/branch/reminder)")
    public ResponseEntity<MobileHomeResponse> getHome(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(homeService.getHome(UUID.fromString(userId)));
    }

    @GetMapping("/nearby-pharmacies")
    @Operation(summary = "Get nearby pharmacies (placeholder - real GPS in production)")
    public ResponseEntity<List<Object>> getNearby(
            @RequestParam(name = "lat", required = false) Double lat,
            @RequestParam(name = "lng", required = false) Double lng) {
        // For MVP, return empty list (PostGIS integration TBD)
        return ResponseEntity.ok(List.of());
    }
}
