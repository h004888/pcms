package com.pcms.customerservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserClient {

    @PutMapping("/users/{id}/profile")
    Map<String, Object> syncProfile(
            @PathVariable("id") UUID id,
            @RequestBody Map<String, Object> request);
}
