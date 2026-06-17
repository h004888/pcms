package com.pcms.reportservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/inventory")
    List<Map<String, Object>> getInventory(@RequestParam(required = false) UUID branchId);

    @GetMapping("/inventory/low-stock")
    List<Map<String, Object>> getLowStock();

    @GetMapping("/inventory/report/stock-level")
    List<Map<String, Object>> getStockLevelReport(@RequestParam(required = false) UUID branchId);

    @GetMapping("/inventory/report/movement")
    List<Map<String, Object>> getMovementReport(@RequestParam(required = false) UUID branchId);
}
