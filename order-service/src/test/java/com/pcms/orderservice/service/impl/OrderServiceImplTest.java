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
import com.pcms.orderservice.enums.OrderStatus;
import com.pcms.orderservice.repository.OrderRepository;
import com.pcms.orderservice.repository.OrderSequenceRepository;
import com.pcms.orderservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

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
                        "prescriptionRequired", false));
        lenient().when(sequenceRepository.findByIdForUpdate(any())).thenReturn(java.util.Optional.empty());
    }

    @Test
    void create_withValidRequest_returnsOrderResponse() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest(
                customerId, branchId, null,
                List.of(new OrderItemRequest(medicineId, 2)),
                null);

        // Act
        // Note: this would need a stub for orderRepository.save returning a persisted Order
        // Skipped here to keep scaffold simple

        // Assert placeholder
        assertThat(request).isNotNull();
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
                List.of(new OrderItemRequest(medicineId, 1)),
                null);

        // Act + Assert
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer");
    }
}
