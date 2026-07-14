package com.pcms.customerportal.controller;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.response.HomePageResponse;
import com.pcms.customerportal.dto.response.ProductDetailResponse;
import com.pcms.customerportal.service.ShopHomeService;
import com.pcms.customerportal.service.ShopPdpService;
import com.pcms.customerportal.service.ShopSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

/**
 * B2C Shop controller - thin, delegates to services.
 *
 * <p>Maps to SDD screens:
 * <ul>
 *   <li>GET /shop/home        → SHOP-HOME</li>
 *   <li>GET /shop/pdp/{id}    → SHOP-PDP</li>
 *   <li>GET /shop/search      → SHOP-SEARCH</li>
 *   <li>GET /shop/lookup/*    → SHOP-LOOKUP-DRUG/INGREDIENT/HERB</li>
 * </ul>
 */
@RestController
@RequestMapping("/shop")
@Tag(name = "UC14 - Customer Portal / Shop")
public class ShopController {

    private final ShopHomeService homeService;
    private final ShopPdpService pdpService;
    private final ShopSearchService searchService;
    private final DataSource dataSource;

    public ShopController(ShopHomeService homeService,
                          ShopPdpService pdpService,
                          ShopSearchService searchService,
                          DataSource dataSource) {
        this.homeService = homeService;
        this.pdpService = pdpService;
        this.searchService = searchService;
        this.dataSource = dataSource;
    }

    @GetMapping(value = "/home", produces = "application/json;charset=UTF-8")
    @Operation(summary = "SHOP-HOME - hero banners, best sellers, categories, videos")
    public ResponseEntity<HomePageResponse> home(
            @RequestParam(name = "customerId", required = false) UUID customerId) {
        return ResponseEntity.ok(homeService.buildHomePage(customerId));
    }

    @GetMapping("/pdp/{id}")
    @Operation(summary = "SHOP-PDP - product detail page")
    public ResponseEntity<ProductDetailResponse> pdp(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(pdpService.getProductDetail(id));
    }

    @GetMapping("/pdp/slug/{slug}")
    @Operation(summary = "SHOP-PDP by slug - product detail by URL slug")
    public ResponseEntity<ProductDetailResponse> pdpBySlug(@PathVariable("slug") String slug) {
        return ResponseEntity.ok(pdpService.getProductDetailBySlug(slug));
    }

    @GetMapping("/search")
    @Operation(summary = "SHOP-SEARCH - search products")
    public ResponseEntity<PageResponse<Map<String, Object>>> search(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.search(q, page, size));
    }

    @GetMapping("/lookup/drug")
    @Operation(summary = "SHOP-LOOKUP-DRUG - lookup medicine by name with A-Z filter")
    public ResponseEntity<PageResponse<Map<String, Object>>> lookupDrug(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(name = "aToZ", required = false) String aToZ,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.lookupDrug(q, aToZ, page, size));
    }

    @GetMapping("/lookup/ingredient")
    @Operation(summary = "SHOP-LOOKUP-INGREDIENT - lookup by active ingredient")
    public ResponseEntity<PageResponse<Map<String, Object>>> lookupIngredient(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.lookupIngredient(q, page, size));
    }

    @GetMapping("/lookup/herb")
    @Operation(summary = "SHOP-LOOKUP-HERB - lookup traditional herbs")
    public ResponseEntity<PageResponse<Map<String, Object>>> lookupHerb(
            @RequestParam(name = "q", required = false, defaultValue = "") String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.lookupHerb(q, page, size));
    }

    @GetMapping("/flash-sales")
    @Operation(summary = "SHOP-FLASH-SALES - active flash sales with items")
    public ResponseEntity<List<Map<String, Object>>> flashSales() {
        var jdbc = new org.springframework.jdbc.core.JdbcTemplate(dataSource);
        var sales = jdbc.queryForList(
            "SELECT fs.id, fs.name, fs.description, fs.discount_pct, " +
            "fs.starts_at, fs.ends_at " +
            "FROM pcms_ecom_ops.flash_sales fs " +
            "WHERE fs.status = 'ACTIVE' AND fs.starts_at <= NOW() AND fs.ends_at > NOW() " +
            "ORDER BY fs.starts_at DESC"
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (var sale : sales) {
            Map<String, Object> s = new HashMap<>(sale);
            var items = jdbc.queryForList(
                "SELECT fsi.id, fsi.medicine_name, fsi.original_price, fsi.sale_price, " +
                "fsi.image_url, fsi.qty_limit, fsi.sold_qty " +
                "FROM pcms_ecom_ops.flash_sale_items fsi " +
                "WHERE fsi.flash_sale_id = ?", s.get("id")
            );
            // Convert byte[] id to string
            s.put("id", s.get("id").toString());
            s.put("items", items);
            result.add(s);
        }
        return ResponseEntity.ok(result);
    }
}