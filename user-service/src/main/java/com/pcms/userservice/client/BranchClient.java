package com.pcms.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

/** Validates that personnel can only be assigned to an operating branch. */
@FeignClient(name = "branch-service")
public interface BranchClient {

    @GetMapping("/branches/{id}")
    Map<String, Object> getBranchById(@PathVariable UUID id);
}
