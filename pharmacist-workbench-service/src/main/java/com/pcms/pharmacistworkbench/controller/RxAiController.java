package com.pcms.pharmacistworkbench.controller;

import com.pcms.pharmacistworkbench.client.AiEngineClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * RX AI Controller - calls ai-engine-service via Feign.
 *
 * <p>Wrapped with try-catch because:
 *  - ai-engine-service (Python FastAPI) may not be running in dev/CI
 *  - Feign fallback doesn't trigger when LoadBalancer throws before HTTP call
 *  - Better to return graceful empty response than 500 to client
 */
@RestController
@RequestMapping("/rx")
@Tag(name = "UC16 - Pharmacist AI Suggestions (RX-CROSS-SELL)")
public class RxAiController {

    private static final Logger log = LoggerFactory.getLogger(RxAiController.class);
    private final AiEngineClient aiEngineClient;

    public RxAiController(AiEngineClient aiEngineClient) {
        this.aiEngineClient = aiEngineClient;
    }

    @PostMapping("/cross-sell")
    @Operation(summary = "Get AI cross-sell suggestions for an order")
    public ResponseEntity<Map<String, Object>> crossSell(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.ok(aiEngineClient.crossSell(request));
        } catch (Exception e) {
            log.warn("ai-engine-service unavailable for cross-sell, returning fallback: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "suggestions", java.util.List.of(),
                "fallback", true,
                "reason", "ai-engine-service unavailable"
            ));
        }
    }

    @PostMapping("/drug-check")
    @Operation(summary = "Check drug interactions for a list of medicine IDs")
    public ResponseEntity<Map<String, Object>> drugCheck(@RequestBody Map<String, Object> request) {
        try {
            return ResponseEntity.ok(aiEngineClient.drugCheck(request));
        } catch (Exception e) {
            log.warn("ai-engine-service unavailable for drug-check, returning fallback: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "interactions", java.util.List.of(),
                "fallback", true,
                "reason", "ai-engine-service unavailable"
            ));
        }
    }
}