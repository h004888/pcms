package com.pcms.orderservice.service;

import com.pcms.orderservice.client.CatalogClient;
import com.pcms.orderservice.client.CustomerClient;
import com.pcms.orderservice.client.InventoryClient;
import com.pcms.orderservice.entity.Order;
import com.pcms.orderservice.entity.OrderItem;
import com.pcms.orderservice.enums.OrderStatus;
import com.pcms.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Business logic for UC06
 * BR04: 5% discount when qty >= 10 same medicine
 * BR01: Auto-cancel pending orders after 24h
 * NSF-05: FIFO batch consumption via inventory-service
 * NSF-12: Generate order number ORD-yyyymmdd-####
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CatalogClient catalogClient;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private CustomerClient customerClient;

    @Value("${order.bulk-discount-threshold:10}")
    private int bulkDiscountThreshold;

    @Value("${order.bulk-discount-rate:0.05}")
    private BigDecimal bulkDiscountRate;

    /**
     * Step 5-12 of UC06 main flow
     */
    @Transactional
    public Order createOrder(UUID customerId, UUID branchId, UUID staffId,
                            List<Map<String, Object>> items, String couponCode) {
        // Step 4-6: Build order lines with medicine info
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setBranchId(branchId);
        order.setStaffId(staffId);
        order.setCouponCode(couponCode);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (Map<String, Object> itemMap : items) {
            UUID medicineId = UUID.fromString(itemMap.get("medicineId").toString());
            Integer qty = Integer.valueOf(itemMap.get("quantity").toString());

            // Call catalog-service to get medicine info
            Map<String, Object> medicine = catalogClient.getMedicineById(medicineId);
            String name = (String) medicine.getOrDefault("name", "Unknown");
            BigDecimal price = new BigDecimal(medicine.get("price").toString());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMedicineId(medicineId);
            item.setMedicineName(name);
            item.setQty(qty);
            item.setUnitPrice(price);

            // Step 6: BR04 bulk discount
            BigDecimal lineSubtotal = price.multiply(BigDecimal.valueOf(qty));
            BigDecimal lineDiscount = BigDecimal.ZERO;
            if (qty >= bulkDiscountThreshold) {
                lineDiscount = lineSubtotal.multiply(bulkDiscountRate).setScale(2, RoundingMode.HALF_UP);
            }
            item.setDiscount(lineDiscount);
            item.setSubtotal(lineSubtotal.subtract(lineDiscount));

            order.getItems().add(item);
            subtotal = subtotal.add(lineSubtotal);
            totalDiscount = totalDiscount.add(lineDiscount);
        }

        order.setSubtotal(subtotal);
        order.setDiscount(totalDiscount);
        order.setTotal(subtotal.subtract(totalDiscount));
        order.setOrderNumber(generateOrderNumber());

        return orderRepository.save(order);
    }

    /**
     * Step 9-12 of UC07 (called by payment-service after payment success)
     * Marks order PAID, consumes stock, awards points
     */
    @Transactional
    public Order markAsPaid(UUID orderId, UUID actorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.COMPLETED) {
            return order;  // idempotent
        }
        order.setStatus(OrderStatus.PAID);

        // Deduct stock via inventory-service (NSF-05 FIFO)
        for (OrderItem item : order.getItems()) {
            try {
                inventoryClient.consumeStock(new InventoryClient.ConsumeRequest(
                    item.getMedicineId(), order.getBranchId(), item.getQty(), actorId, order.getId()));
            } catch (Exception e) {
                log.warn("Failed to consume stock for medicine {} in order {}: {}",
                    item.getMedicineId(), order.getId(), e.getMessage());
            }
        }

        // BR07: Award loyalty points
        try {
            int points = order.getTotal().divide(BigDecimal.valueOf(1000), 0, RoundingMode.FLOOR).intValue();
            if (points > 0) {
                customerClient.addPoints(order.getCustomerId(), Map.of("points", points));
            }
        } catch (Exception e) {
            log.warn("Failed to award loyalty points for order {}: {}", order.getId(), e.getMessage());
        }

        return orderRepository.save(order);
    }

    /**
     * AT2 of UC06: Cancel order, restore stock (BR06)
     */
    @Transactional
    public Order cancelOrder(UUID orderId, UUID actorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed order");
        }
        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        // BR06: Restore stock only if previously PAID
        if (previousStatus == OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                try {
                    inventoryClient.consumeStock(new InventoryClient.ConsumeRequest(
                        item.getMedicineId(), order.getBranchId(), -item.getQty(), actorId, order.getId()));
                } catch (Exception e) {
                    log.warn("Failed to restore stock for order {}: {}", order.getId(), e.getMessage());
                }
            }
        }
        return orderRepository.save(order);
    }

    /**
     * NSF-12: Generate order number ORD-yyyymmdd-####
     */
    private String generateOrderNumber() {
        String datePrefix = LocalDate.now().format(DATE_FMT);
        List<Order> latest = orderRepository.findByDatePrefix(datePrefix, PageRequest.of(0, 1));
        int nextNum = 1;
        if (!latest.isEmpty()) {
            String numPart = latest.get(0).getOrderNumber().substring(latest.get(0).getOrderNumber().lastIndexOf('-') + 1);
            try { nextNum = Integer.parseInt(numPart) + 1; } catch (NumberFormatException ignored) {}
        }
        return String.format("ORD-%s-%04d", datePrefix, nextNum);
    }
}
