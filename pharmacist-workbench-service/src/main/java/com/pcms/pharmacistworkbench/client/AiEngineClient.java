package com.pcms.pharmacistworkbench.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign client for ai-engine-service (Python FastAPI).
 * Used for AI-powered suggestions like cross-sell, drug interaction checks.
 */
@FeignClient(name = "ai-engine-service", fallback = AiEngineClient.Fallback.class, configuration = com.pcms.pharmacistworkbench.config.FeignMapConfig.class)
public interface AiEngineClient {

    @PostMapping("/api/v1/ai/cross-sell")
    Map<String, Object> crossSell(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/ai/drug-check")
    Map<String, Object> drugCheck(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/ai/summary")
    Map<String, Object> summary(@RequestBody Map<String, Object> request);

    class Fallback implements AiEngineClient {
        @Override
        public Map<String, Object> crossSell(Map<String, Object> request) {
            return Map.of("suggestions", java.util.List.of(), "fallback", true);
        }
        @Override
        public Map<String, Object> drugCheck(Map<String, Object> request) {
            return Map.of("interactions", java.util.List.of(), "fallback", true);
        }
        @Override
        public Map<String, Object> summary(Map<String, Object> request) {
            return Map.of("summary", "AI summary unavailable", "fallback", true);
        }
    }
}
