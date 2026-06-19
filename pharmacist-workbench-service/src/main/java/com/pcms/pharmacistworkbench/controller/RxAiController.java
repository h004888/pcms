package com.pcms.pharmacistworkbench.controller;

import com.pcms.pharmacistworkbench.client.AiEngineClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rx")
@Tag(name = "UC16 - Pharmacist AI Suggestions (RX-CROSS-SELL)")
public class RxAiController {

    private final AiEngineClient aiEngineClient;

    public RxAiController(AiEngineClient aiEngineClient) {
        this.aiEngineClient = aiEngineClient;
    }

    @PostMapping("/cross-sell")
    @Operation(summary = "Get AI cross-sell suggestions for an order")
    public ResponseEntity<Map<String, Object>> crossSell(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(aiEngineClient.crossSell(request));
    }

    @PostMapping("/drug-check")
    @Operation(summary = "Check drug interactions for a list of medicine IDs")
    public ResponseEntity<Map<String, Object>> drugCheck(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(aiEngineClient.drugCheck(request));
    }
}
