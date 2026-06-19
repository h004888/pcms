package com.pcms.mobilebff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "notification-service", fallback = NotificationClient.Fallback.class)
public interface NotificationClient {
    @GetMapping("/api/v1/notifications")
    Map<String, Object> list(@RequestParam("customerId") String customerId,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "10") int size);

    class Fallback implements NotificationClient {
        @Override
        public Map<String, Object> list(String customerId, int page, int size) {
            return Map.of("data", java.util.List.of());
        }
    }
}
