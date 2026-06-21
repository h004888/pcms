package com.pcms.orderservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.orderservice.client.CatalogClient;
import com.pcms.orderservice.client.CustomerClient;
import com.pcms.orderservice.client.InventoryClient;
import com.pcms.orderservice.client.PrescriptionClient;
import com.pcms.orderservice.entity.Coupon;
import com.pcms.orderservice.dto.CreateOrderRequest;
import com.pcms.orderservice.dto.OrderItemRequest;
import com.pcms.orderservice.dto.OrderRecomputeResponse;
import com.pcms.orderservice.dto.OrderResponse;
import com.pcms.orderservice.dto.UpdateOrderRequest;
import com.pcms.orderservice.entity.Order;
import com.pcms.orderservice.entity.OrderItem;
import com.pcms.orderservice.entity.OrderSequence;
import com.pcms.orderservice.enums.OrderStatus;
import com.pcms.orderservice.repository.OrderRepository;
import com.pcms.orderservice.repository.OrderSequenceRepository;
import com.pcms.orderservice.repository.SagaInstanceRepository;
import com.pcms.orderservice.saga.SagaCompensationHandler;
import com.pcms.orderservice.saga.SagaOrchestrator;
import com.pcms.orderservice.service.OrderService;
import com.pcms.orderservice.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link OrderService}.
 *
 * <p>
 * BR04: 5% discount when qty >= 10 same medicine.
 * <br>
 * BR01: Auto-cancel pending orders after 24h.
 * <br>
 * NSF-05: FIFO batch consumption via inventory-service.
 * <br>
 * NSF-12: Generate order number ORD-yyyymmdd-####.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final CustomerClient customerClient;
    private final PrescriptionClient prescriptionClient;
    private final com.pcms.orderservice.client.BranchClient branchClient;
    private final com.pcms.orderservice.repository.OrderSequenceRepository sequenceRepository;
    private final com.pcms.orderservice.repository.OutboxEventRepository outboxRepo;
    private final CouponService couponService;
    private final SagaOrchestrator sagaOrchestrator;
    private final SagaCompensationHandler sagaCompensationHandler;
    private final SagaInstanceRepository sagaRepo;

    @Value("${order.bulk-discount-threshold:10}")
    private int bulkDiscountThreshold;

    @Value("${order.bulk-discount-rate:0.05}")
    private BigDecimal bulkDiscountRate;

    public OrderServiceImpl(OrderRepository orderRepository,
            CatalogClient catalogClient,
            InventoryClient inventoryClient,
            CustomerClient customerClient,
            PrescriptionClient prescriptionClient,
            com.pcms.orderservice.client.BranchClient branchClient,
            com.pcms.orderservice.repository.OrderSequenceRepository sequenceRepository,
            com.pcms.orderservice.repository.OutboxEventRepository outboxRepo,
            CouponService couponService,
            SagaOrchestrator sagaOrchestrator,
            SagaCompensationHandler sagaCompensationHandler,
            SagaInstanceRepository sagaRepo) {
        this.orderRepository = orderRepository;
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
        this.customerClient = customerClient;
        this.prescriptionClient = prescriptionClient;
        this.branchClient = branchClient;
        this.sequenceRepository = sequenceRepository;
        this.outboxRepo = outboxRepo;
        this.couponService = couponService;
        this.sagaOrchestrator = sagaOrchestrator;
        this.sagaCompensationHandler = sagaCompensationHandler;
        this.sagaRepo = sagaRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> list(UUID customerId, OrderStatus status, int page, int size) {
        return list(customerId, status, null, null, null, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> list(UUID customerId, OrderStatus status, UUID branchId,
            LocalDate dateFrom, LocalDate dateTo, int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new InvalidOperationException(
                    "dateFrom must be before dateTo",
                    "Ngày bắt đầu phải trước ngày kết thúc");
        }
        var fromDateTime = dateFrom == null ? null : dateFrom.atStartOfDay();
        var toDateTime = dateTo == null ? null : dateTo.plusDays(1).atStartOfDay().minusNanos(1);
        Page<Order> orders = orderRepository.search(customerId, status, branchId, fromDateTime, toDateTime, pageable);
        return orders.map(OrderResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderNumber));
    }

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new InvalidOperationException(
                    "Order must contain at least one item",
                    "Đơn hàng phải có ít nhất một sản phẩm");
        }

        // B-20: Validate customer exists via customer-service
        try {
            var customer = customerClient.getCustomerById(request.customerId());
            if (customer == null || customer.get("status") == null
                    || "UNREACHABLE".equals(customer.get("status"))) {
                throw new ResourceNotFoundException("Customer", request.customerId());
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("Customer", request.customerId());
        }
        // B-21: Validate branch exists via branch-service
        try {
            var branch = branchClient.getBranchById(request.branchId());
            if (branch == null || branch.get("status") == null
                    || "UNREACHABLE".equals(branch.get("status"))) {
                throw new ResourceNotFoundException("Branch", request.branchId());
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("Branch", request.branchId());
        }

        Order order = new Order();
        order.setCustomerId(request.customerId());
        order.setBranchId(request.branchId());
        order.setStaffId(request.staffId());
        order.setPrescriptionId(request.prescriptionId());
        order.setCouponCode(request.couponCode());
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        boolean requiresPrescription = false;

        for (OrderItemRequest itemReq : request.items()) {
            // Call catalog-service to get medicine info
            Map<String, Object> medicine = catalogClient.getMedicineById(itemReq.medicineId());
            String name = (String) medicine.getOrDefault("name", "Unknown");
            // If catalog fallback returned a placeholder (id matches but name="Unknown"),
            // the medicine was not found.
            if ("Unknown".equals(name) || medicine.get("price") == null
                    || BigDecimal.ZERO.compareTo(new BigDecimal(medicine.get("price").toString())) == 0) {
                throw new ResourceNotFoundException("Medicine", itemReq.medicineId());
            }
            BigDecimal price = new BigDecimal(medicine.get("price").toString());
            requiresPrescription = requiresPrescription || isPrescriptionRequired(medicine);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMedicineId(itemReq.medicineId());
            item.setMedicineName(name);
            item.setQty(itemReq.quantity());
            item.setUnitPrice(price);

            // BR04 bulk discount
            BigDecimal lineSubtotal = price.multiply(BigDecimal.valueOf(itemReq.quantity()));
            BigDecimal lineDiscount = BigDecimal.ZERO;
            if (itemReq.quantity() >= bulkDiscountThreshold) {
                lineDiscount = lineSubtotal.multiply(bulkDiscountRate).setScale(2, RoundingMode.HALF_UP);
            }
            item.setDiscount(lineDiscount);
            item.setSubtotal(lineSubtotal.subtract(lineDiscount));

            order.getItems().add(item);
            subtotal = subtotal.add(lineSubtotal);
            totalDiscount = totalDiscount.add(lineDiscount);
        }

        if (requiresPrescription) {
            validatePrescription(request.prescriptionId(), request.customerId());
        } else if (request.prescriptionId() != null) {
            validatePrescription(request.prescriptionId(), request.customerId());
        }

        BigDecimal baseTotal = subtotal.subtract(totalDiscount);
        Coupon appliedCoupon = couponService.findApplicableCoupon(request.couponCode());
        BigDecimal couponDiscount = couponService.calculateDiscount(appliedCoupon, baseTotal);

        order.setSubtotal(subtotal);
        order.setDiscount(totalDiscount.add(couponDiscount));
        order.setTotal(baseTotal.subtract(couponDiscount));
        order.setOrderNumber(generateOrderNumber());

        if (appliedCoupon != null) {
            order.setCouponCode(appliedCoupon.getCode());
            couponService.incrementUsage(appliedCoupon);
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    private boolean isPrescriptionRequired(Map<String, Object> medicine) {
        Object value = medicine.get("prescriptionRequired");
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return value instanceof String text && Boolean.parseBoolean(text);
    }

    private void validatePrescription(UUID prescriptionId, UUID customerId) {
        if (prescriptionId == null) {
            throw new InvalidOperationException(
                    "Prescription is required for at least one medicine",
                    "Đơn thuốc là bắt buộc vì có thuốc cần kê đơn");
        }
        try {
            Map<String, Object> prescription = prescriptionClient.getPrescriptionById(prescriptionId);
            if (prescription == null || "UNREACHABLE".equals(prescription.get("status"))) {
                throw new ResourceNotFoundException("Prescription", prescriptionId);
            }
            if (!"SIGNED".equalsIgnoreCase(String.valueOf(prescription.get("status")))) {
                throw new InvalidOperationException(
                        "Prescription must be signed",
                        "Đơn thuốc phải được ký trước khi tạo đơn hàng");
            }
            Object patientId = prescription.get("patientId");
            if (patientId != null && !customerId.toString().equalsIgnoreCase(String.valueOf(patientId))) {
                throw new InvalidOperationException(
                        "Prescription patient does not match order customer",
                        "Bệnh nhân trong đơn thuốc không khớp với khách hàng của đơn hàng");
            }
        } catch (feign.FeignException.NotFound e) {
            throw new ResourceNotFoundException("Prescription", prescriptionId);
        }
    }

    @Override
    @Transactional
    public OrderResponse update(UUID id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOperationException(
                    "Order can only be updated while PENDING_PAYMENT",
                    "Đơn hàng chỉ có thể cập nhật khi đang chờ thanh toán");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new InvalidOperationException(
                    "Order must contain at least one item",
                    "Đơn hàng phải có ít nhất một sản phẩm");
        }
        // Replace items and recompute totals
        order.getItems().clear();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.items()) {
            Map<String, Object> medicine = catalogClient.getMedicineById(itemReq.medicineId());
            String name = (String) medicine.getOrDefault("name", "Unknown");
            if ("Unknown".equals(name) || medicine.get("price") == null
                    || BigDecimal.ZERO.compareTo(new BigDecimal(medicine.get("price").toString())) == 0) {
                throw new ResourceNotFoundException("Medicine", itemReq.medicineId());
            }
            BigDecimal price = new BigDecimal(medicine.get("price").toString());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMedicineId(itemReq.medicineId());
            item.setMedicineName(name);
            item.setQty(itemReq.quantity());
            item.setUnitPrice(price);

            BigDecimal lineSubtotal = price.multiply(BigDecimal.valueOf(itemReq.quantity()));
            BigDecimal lineDiscount = BigDecimal.ZERO;
            if (itemReq.quantity() >= bulkDiscountThreshold) {
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
        return OrderResponse.from(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse markAsPaid(UUID orderId, UUID actorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.APPROVED) {
            throw new InvalidOperationException(
                    "Order can only be marked paid while PENDING_PAYMENT or APPROVED",
                    "Đơn hàng chỉ có thể chuyển PAID khi đang chờ thanh toán hoặc đã duyệt");
        }
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // B-17: Orchestration saga — replaces 3 separate outbox events with a single
        // saga that has automatic compensation on failure and stuck-saga detection.
        sagaOrchestrator.startOrderFulfillment(order, actorId);

        return OrderResponse.from(order);
    }

    @Override
    @Transactional
    public OrderResponse approve(UUID orderId, UUID actorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOperationException(
                    "Only pending-payment orders can be approved",
                    "Chỉ có thể duyệt đơn hàng đang chờ thanh toán");
        }
        order.setStatus(OrderStatus.APPROVED);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse reject(UUID orderId, UUID actorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT && order.getStatus() != OrderStatus.APPROVED) {
            throw new InvalidOperationException(
                    "Only pending or approved orders can be rejected",
                    "Chỉ có thể từ chối đơn hàng đang chờ hoặc đã duyệt");
        }
        order.setStatus(OrderStatus.REJECTED);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID orderId, UUID actorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOperationException(
                    "Cannot cancel completed order",
                    "Không thể hủy đơn hàng đã hoàn tất");
        }
        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        // B-17: Trigger saga compensation if a saga exists for this order.
        var sagaOpt = sagaRepo.findByAggregateTypeAndAggregateId("Order", orderId);
        if (sagaOpt.isPresent()) {
            sagaCompensationHandler.compensate(sagaOpt.get().getId(),
                    "Manual cancellation by user/actor " + actorId);
        } else if (previousStatus == OrderStatus.PAID) {
            // Backwards-compat: keep the original STOCK_RESTORED outbox event for old orders
            // without a saga (e.g., orders placed before the saga migration).
            for (OrderItem item : order.getItems()) {
                String payload = String.format(
                        "{\"medicineId\":\"%s\",\"branchId\":\"%s\",\"qty\":%d,\"orderId\":\"%s\",\"actorId\":\"%s\"}",
                        item.getMedicineId(), order.getBranchId(), item.getQty(), order.getId(), actorId);
                outboxRepo.save(new com.pcms.orderservice.entity.OutboxEvent(
                        "Order", order.getId(), "STOCK_RESTORED",
                        "inventory-service", "/inventory/orders/" + order.getId() + "/cancelled", payload));
            }
        }
        return OrderResponse.from(saved);
    }

    /**
     * TICKET-203: SDD §6.8 POST /orders/{id}/recompute.
     * Re-applies BR04 bulk discount (5% when qty >= 10 same medicine)
     * and checks stock availability via inventory-service.
     *
     * <p>Only recomputes for PENDING_PAYMENT orders (other states are
     * frozen — recompute is a pre-place helper).
     *
     * <p>Does NOT persist the new totals; the caller is expected to call
     * update() to materialize them, or simply place the order via create().
     */
    @Override
    @Transactional(readOnly = true)
    public OrderRecomputeResponse recompute(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new InvalidOperationException(
                    "Only PENDING_PAYMENT orders can be recomputed",
                    "Chỉ có thể tính lại đơn hàng đang chờ thanh toán");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        List<OrderRecomputeResponse.StockWarning> stockWarnings = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            // Line discount: BR04 5% when qty >= 10
            BigDecimal lineSubtotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQty()));
            BigDecimal lineDiscount = BigDecimal.ZERO;
            if (item.getQty() >= bulkDiscountThreshold) {
                lineDiscount = lineSubtotal.multiply(bulkDiscountRate)
                        .setScale(2, RoundingMode.HALF_UP);
            }
            subtotal = subtotal.add(lineSubtotal);
            totalDiscount = totalDiscount.add(lineDiscount);

            // Stock check via inventory-service (best-effort; tolerant of unavailable service)
            try {
                Map<String, Object> stock = inventoryClient.getAvailableStock(
                        item.getMedicineId(), order.getBranchId());
                if (stock != null && stock.get("availableQty") != null) {
                    int available = ((Number) stock.get("availableQty")).intValue();
                    if (available < item.getQty()) {
                        String severity = available == 0 ? "BLOCK"
                                : (available < item.getQty() / 2 ? "WARNING" : "INFO");
                        stockWarnings.add(new OrderRecomputeResponse.StockWarning(
                                item.getMedicineId(),
                                item.getMedicineName(),
                                item.getQty(),
                                available,
                                severity));
                    }
                }
            } catch (Exception e) {
                // Inventory service unavailable — emit INFO warning so caller knows
                log.warn("Inventory check failed for medicine {}: {}",
                        item.getMedicineId(), e.getMessage());
                stockWarnings.add(new OrderRecomputeResponse.StockWarning(
                        item.getMedicineId(),
                        item.getMedicineName(),
                        item.getQty(),
                        -1, // unknown
                        "INFO"));
            }
        }

        // Apply coupon on top of BR04 (matches create() flow)
        BigDecimal baseTotal = subtotal.subtract(totalDiscount);
        Coupon appliedCoupon = couponService.findApplicableCoupon(order.getCouponCode());
        BigDecimal couponDiscount = couponService.calculateDiscount(appliedCoupon, baseTotal);
        BigDecimal finalDiscount = totalDiscount.add(couponDiscount);

        // Update in-memory entity for the response (not persisted)
        order.setSubtotal(subtotal);
        order.setDiscount(finalDiscount);
        order.setTotal(baseTotal.subtract(couponDiscount));

        return OrderRecomputeResponse.from(order, stockWarnings);
    }

    /**
     * B-08: Generate next order number using a dedicated sequence table to avoid
     * race condition.
     * Falls back to the legacy scan-based method if sequence table not present.
     * <p>
     * Sequence row is locked with PESSIMISTIC_WRITE so concurrent calls serialize.
     */
    private String generateOrderNumber() {
        String datePrefix = LocalDate.now().format(DATE_FMT);
        // Use SELECT ... FOR UPDATE to lock the row and serialize concurrent calls
        Optional<OrderSequence> seqOpt = sequenceRepository.findByIdForUpdate(datePrefix);
        OrderSequence seq;
        if (seqOpt.isPresent()) {
            seq = seqOpt.get();
            seq.setLastSeq(seq.getLastSeq() + 1);
        } else {
            // First order of the day — check if any prior order exists (for migration)
            int next = 1;
            List<Order> latest = orderRepository.findByDatePrefix(datePrefix, PageRequest.of(0, 1));
            if (!latest.isEmpty()) {
                String numPart = latest.get(0).getOrderNumber()
                        .substring(latest.get(0).getOrderNumber().lastIndexOf('-') + 1);
                try {
                    next = Integer.parseInt(numPart) + 1;
                } catch (NumberFormatException ignored) {
                }
            }
            seq = new OrderSequence(datePrefix, next);
        }
        sequenceRepository.save(seq);
        return String.format("ORD-%s-%04d", datePrefix, seq.getLastSeq());
    }
}
