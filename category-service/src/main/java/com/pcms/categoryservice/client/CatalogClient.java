package com.pcms.categoryservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/medicines/count")
    long countMedicinesByCategory(@RequestParam("categoryId") UUID categoryId);
}