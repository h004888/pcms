package com.pcms.catalogservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "category-service")
public interface CategoryClient {

    @GetMapping("/categories/{id}")
    Map<String, Object> getById(@PathVariable("id") UUID id);
}