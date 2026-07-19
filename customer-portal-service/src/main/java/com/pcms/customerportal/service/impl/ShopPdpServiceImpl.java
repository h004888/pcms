package com.pcms.customerportal.service.impl;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.customerportal.client.CatalogClient;
import com.pcms.customerportal.client.InventoryClient;
import com.pcms.customerportal.dto.response.ProductDetailResponse;
import com.pcms.customerportal.dto.response.ProductDetailResponse.CategoryRef;
import com.pcms.customerportal.dto.response.ProductDetailResponse.RelatedProduct;
import com.pcms.customerportal.dto.response.ProductDetailResponse.StockByBranch;
import com.pcms.customerportal.repository.ProductReviewRepository;
import com.pcms.customerportal.service.ShopPdpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of ShopPdpService.
 *
 * <p>All upstream failures (catalog, inventory) are isolated so PDP
 * returns a partial but useful response. Reviews and related products
 * are computed locally.
 */
@Service
public class ShopPdpServiceImpl implements ShopPdpService {

    private static final Logger log = LoggerFactory.getLogger(ShopPdpServiceImpl.class);

    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final ProductReviewRepository reviewRepo;

    public ShopPdpServiceImpl(CatalogClient catalogClient,
                              InventoryClient inventoryClient,
                              ProductReviewRepository reviewRepo) {
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
        this.reviewRepo = reviewRepo;
    }

    @Override
    public ProductDetailResponse getProductDetail(UUID medicineId) {
        log.debug("Building SHOP-PDP for medicine {}", medicineId);

        Map<String, Object> medicine = catalogClient.getById(medicineId.toString());
        if (medicine == null || medicine.isEmpty()) {
            throw new ResourceNotFoundException("MSG31", "Medicine not found: " + medicineId);
        }

        return buildResponse(medicine);
    }

    @Override
    public ProductDetailResponse getProductDetailBySlug(String slug) {
        log.debug("Building SHOP-PDP by slug {}", slug);

        Map<String, Object> medicine = catalogClient.getBySlug(slug);
        if (medicine == null || medicine.isEmpty()) {
            throw new ResourceNotFoundException("MSG31", "Medicine not found by slug: " + slug);
        }

        return buildResponse(medicine);
    }

    private ProductDetailResponse buildResponse(Map<String, Object> medicine) {
        UUID medicineId = UUID.fromString(str(medicine.get("id")));

        Double avgRating = reviewRepo.averageRating(medicineId);
        Long reviewCount = reviewRepo.countByMedicineIdAndStatus(medicineId, "APPROVED");
        List<StockByBranch> stock = loadStock(medicineId);
        String categoryName = str(medicine.get("categoryName"));
        List<RelatedProduct> related = loadRelated(medicineId, categoryName);

        return new ProductDetailResponse(
                str(medicine.get("id")),
                str(medicine.get("sku")),
                str(medicine.get("slug")),
                str(medicine.get("name")),
                new CategoryRef(
                        str(medicine.get("categoryId")),
                        str(medicine.get("categorySlug")),
                        str(medicine.get("categoryName"))),
                toBigDecimal(medicine.get("price")),
                str(medicine.get("unit")),
                str(medicine.get("imageUrl")),
                str(medicine.get("description")),
                List.of(), // ingredients: catalog-service has no list yet
                str(medicine.get("usage")),
                Boolean.TRUE.equals(medicine.get("prescriptionRequired")),
                avgRating,
                reviewCount,
                stock,
                related
        );
    }

    private List<StockByBranch> loadStock(UUID medicineId) {
        try {
            // We aggregate across all branches by calling list() and filtering on client.
            // A more efficient approach would be to add a dedicated "stock-by-medicine"
            // endpoint on inventory-service in a later sprint.
            List<Map<String, Object>> all = inventoryClient.list(null);
            return all.stream()
                    .filter(m -> medicineId.toString().equals(str(m.get("medicineId"))))
                    .map(m -> new StockByBranch(
                            str(m.get("branchId")),
                            str(m.get("branchName")),
                            toInt(m.get("qtyOnHand"))))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load stock for medicine {}: {}", medicineId, e.getMessage());
            return List.of();
        }
    }

    private List<RelatedProduct> loadRelated(UUID medicineId, String categoryName) {
        try {
            List<Map<String, Object>> results = catalogClient.searchMedicines(categoryName, 0, 10);
            if (results == null) return List.of();
            return results.stream()
                    .filter(m -> !medicineId.toString().equals(str(m.get("id"))))
                    .limit(5)
                    .map(m -> new RelatedProduct(
                            str(m.get("id")),
                            str(m.get("slug")),
                            str(m.get("name")),
                            toBigDecimal(m.get("price")),
                            str(m.get("imageUrl"))))
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load related products for medicine {}: {}", medicineId, e.getMessage());
            return List.of();
        }
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return new BigDecimal(n.toString());
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return null; }
    }

    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return null; }
    }
}