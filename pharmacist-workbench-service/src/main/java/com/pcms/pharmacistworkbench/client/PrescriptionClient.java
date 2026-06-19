package com.pcms.pharmacistworkbench.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "prescription-service", fallback = PrescriptionClient.Fallback.class)
public interface PrescriptionClient {

    @GetMapping("/api/v1/prescriptions/{id}")
    Map<String, Object> getById(@PathVariable("id") String id);

    @GetMapping("/api/v1/prescriptions/code/{code}")
    Map<String, Object> getByCode(@PathVariable("code") String code);

    class Fallback implements PrescriptionClient {
        @Override
        public Map<String, Object> getById(String id) { return Map.of(); }
        @Override
        public Map<String, Object> getByCode(String code) { return Map.of(); }
    }
}
