package com.pcms.mobilebff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "branch-service", fallback = BranchClient.Fallback.class)
public interface BranchClient {
    @GetMapping("/api/v1/branches")
    Map<String, Object> list(@RequestParam(name = "page", defaultValue = "0") int page,
                             @RequestParam(name = "size", defaultValue = "20") int size);

    class Fallback implements BranchClient {
        @Override
        public Map<String, Object> list(int page, int size) {
            return Map.of("data", java.util.List.of());
        }
    }
}
