package com.pcms.orderservice.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.orderservice.client.BranchClient;
import com.pcms.orderservice.client.CatalogClient;
import com.pcms.orderservice.client.CustomerClient;
import com.pcms.orderservice.client.InventoryClient;
import com.pcms.orderservice.dto.CreateOrderRequest;
import com.pcms.orderservice.dto.OrderItemRequest;
import com.pcms.orderservice.entity.Order;
import com.pcms.orderservice.entity.OrderStatusHistory;
import com.pcms.orderservice.enums.OrderStatus;
import com.pcms.orderservice.repository.OrderRepository;
import com.pcms.orderservice.repository.OrderSequenceRepository;
import com.pcms.orderservice.repository.OutboxEventRepository;
import com.pcms.orderservice.repository.OrderStatusHistoryRepository;
import com.pcms.orderservice.repository.SagaInstanceRepository;
import com.pcms.orderservice.saga.SagaCompensationHandler;
import com.pcms.orderservice.saga.SagaOrchestrator;
import com.pcms.orderservice.service.CouponService;
import com.pcms.orderservice.client.PrescriptionClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/**
 * Unit test scaffold for OrderServiceImpl (B-18).
 * <p>Pattern: JUnit 5 + Mockito + AssertJ + AAA (Arrange-Act-Assert).
 * <p>This file demonstrates the testing convention for PCMS. Copy + adapt
 * to other services.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CatalogClient catalogClient;
    @Mock private InventoryClient inventoryClient;
    @Mock private CustomerClient customerClient;
    @Mock private BranchClient branchClient;
    @Mock private OrderSequenceRepository sequenceRepository;
    @Mock private OutboxEventRepository outboxRepository;
    @Mock private PrescriptionClient prescriptionClient;
    @Mock private CouponService couponService;
    @Mock private SagaOrchestrator sagaOrchestrator;
    @Mock private SagaCompensationHandler sagaCompensationHandler;
    @Mock private SagaInstanceRepository sagaRepository;
    @Mock private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @InjectMocks private OrderServiceImpl orderService;

    private UUID customerId;
    private UUID branchId;
    private UUID medicineId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        medicineId = UUID.randomUUID();

        // Default mocks: customer + branch exist
        lenient().when(customerClient.getCustomerById(customerId))
                .thenReturn(Map.of("id", customerId.toString(), "status", "ACTIVE"));
        lenient().when(branchClient.getBranchById(branchId))
                .thenReturn(Map.of("id", branchId.toString(), "status", "ACTIVE"));
        lenient().when(catalogClient.getMedicineById(medicineId))
                .thenReturn(Map.of(
                        "id", medicineId.toString(),
                        "name", "Panadol 500mg",
                        "price", BigDecimal.valueOf(45000),
                        "prescriptionRequired", false));        lenient().when(sequenceRepository.findByIdForUpdate(any())).thenReturn(java.util.Optional.empty());
        ReflectionTestUtils.setField(orderService, "bulkDiscountThreshold", 10);
        ReflectionTestUtils.setField(orderService, "bulkDiscountRate", BigDecimal.valueOf(0.05));
    }

    @Test
    void create_withValidRequest_returnsOrderResponse() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
                customerId, branchId, null,
                List.of(new OrderItemRequest(medicineId, 2, null)),
                null);

        Order saved = new Order();
        UUID orderId = UUID.randomUUID();
        saved.setId(orderId);
        saved.setCustomerId(customerId);
        saved.setBranchId(branchId);
        saved.setStatus(OrderStatus.PENDING_PAYMENT);
        when(couponService.findApplicableCoupon(null)).thenReturn(null);
        when(couponService.calculateDiscount(any(), any(BigDecimal.class))).thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        // Act
        orderService.create(request);

        // Assert placeholder
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    void create_withProvidedUnitPrice_usesProvidedPriceNotCatalog() {
        // Arrange — unitPrice=99999, catalog returns price=45000
        BigDecimal providedPrice = BigDecimal.valueOf(99999);
        CreateOrderRequest request = new CreateOrderRequest(
                customerId, branchId, null,
                List.of(new OrderItemRequest(medicineId, 2, providedPrice)),
                null);

        Order saved = new Order();
        UUID orderId = UUID.randomUUID();
        saved.setId(orderId);
        saved.setCustomerId(customerId);
        saved.setBranchId(branchId);
        saved.setStatus(OrderStatus.PENDING_PAYMENT);
        when(couponService.findApplicableCoupon(null)).thenReturn(null);
        when(couponService.calculateDiscount(any(), any(BigDecimal.class))).thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        // Act
        orderService.create(request);

        // Assert — saved OrderItem must have unitPrice from request (99999), not catalog (45000)
        verify(orderRepository).save(argThat(order ->
                order.getItems().get(0).getUnitPrice().compareTo(providedPrice) == 0));
    }

    @Test
    void create_withNullUnitPrice_fallsBackToCatalog() {
        // Arrange — unitPrice is null, should use catalog price=45000
        CreateOrderRequest request = new CreateOrderRequest(
                customerId, branchId, null,
                List.of(new OrderItemRequest(medicineId, 2, null)),
                null);

        Order saved = new Order();
        UUID orderId = UUID.randomUUID();
        saved.setId(orderId);
        saved.setCustomerId(customerId);
        saved.setBranchId(branchId);
        saved.setStatus(OrderStatus.PENDING_PAYMENT);
        when(couponService.findApplicableCoupon(null)).thenReturn(null);
        when(couponService.calculateDiscount(any(), any(BigDecimal.class))).thenReturn(BigDecimal.ZERO);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        // Act
        orderService.create(request);

        // Assert — should fall back to catalog price
        verify(orderRepository).save(argThat(order ->
                order.getItems().get(0).getUnitPrice().compareTo(BigDecimal.valueOf(45000)) == 0));
    }

    @Test
    void create_withEmptyItems_throwsInvalidOperationException() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
                customerId, branchId, null, List.of(), null);

        // Act + Assert
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    void create_withNonExistentCustomer_throwsResourceNotFoundException() {
        // Arrange — override default mock to return "UNREACHABLE" so validation fails
        when(customerClient.getCustomerById(customerId))
                .thenReturn(Map.of("id", customerId.toString(), "status", "UNREACHABLE"));
        CreateOrderRequest request = new CreateOrderRequest(
                customerId, branchId, null,
                List.of(new OrderItemRequest(medicineId, 1, null)),
                null);

        // Act + Assert
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer");
    }

    @Test
    void approve_recordsStatusHistory() {
        Order order = orderWithStatus(OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(order.getId())).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.approve(order.getId(), customerId);

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    void markAsPaid_recordsStatusHistory() {
        Order order = orderWithStatus(OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(order.getId())).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.markAsPaid(order.getId(), customerId);

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void reject_recordsStatusHistory() {
        Order order = orderWithStatus(OrderStatus.APPROVED);
        when(orderRepository.findById(order.getId())).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.reject(order.getId(), customerId);

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);
    }

    @Test
    void cancel_recordsManualStatusHistory() {
        Order order = orderWithStatus(OrderStatus.PENDING_PAYMENT);
        when(orderRepository.findById(order.getId())).thenReturn(java.util.Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(sagaRepository.findByAggregateTypeAndAggregateId("Order", order.getId()))
                .thenReturn(java.util.Optional.empty());

        orderService.cancel(order.getId(), customerId);

        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    private Order orderWithStatus(OrderStatus status) {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerId(customerId);
        order.setBranchId(branchId);
        order.setStatus(status);
        return order;
    }
}
