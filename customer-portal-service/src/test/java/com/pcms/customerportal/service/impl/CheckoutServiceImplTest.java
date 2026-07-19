package com.pcms.customerportal.service.impl;

import com.pcms.common.exception.InvalidOperationException;
import com.pcms.customerportal.client.OrderClient;
import com.pcms.customerportal.client.PaymentServiceClient;
import com.pcms.customerportal.dto.request.CheckoutConfirmRequest;
import com.pcms.customerportal.dto.request.CheckoutPreviewRequest;
import com.pcms.customerportal.dto.response.CheckoutConfirmResponse;
import com.pcms.customerportal.entity.Cart;
import com.pcms.customerportal.entity.CartItem;
import com.pcms.customerportal.enums.CartStatus;
import com.pcms.customerportal.repository.CartItemRepository;
import com.pcms.customerportal.repository.CartRepository;
import com.pcms.customerportal.service.CartFactory;
import com.pcms.customerportal.service.VoucherService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private VoucherService voucherService;
    @Mock private OrderClient orderClient;
    @Mock private PaymentServiceClient paymentServiceClient;
    @Mock private CartFactory cartFactory;
    @InjectMocks private CheckoutServiceImpl checkoutService;

    private UUID customerId;
    private UUID branchId;
    private Cart cart;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setCustomerId(customerId);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setTotal(BigDecimal.valueOf(50000));
    }

    @Test
    void confirm_whenNoActiveCart_autoCreatesCart() {
        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());
        item.setMedicineId(UUID.randomUUID());
        item.setQty(2);
        item.setMedicineName("Panadol");
        item.setUnitPrice(BigDecimal.valueOf(25000));

        UUID orderId = UUID.randomUUID();

        CheckoutConfirmRequest request = new CheckoutConfirmRequest(
                UUID.randomUUID(), branchId, "pickup", null, "COD");

        when(cartFactory.getOrCreateCart(customerId)).thenReturn(cart);
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(item));
        when(orderClient.createOrder(any(Map.class)))
                .thenReturn(Map.of("id", orderId.toString(), "orderNumber", "ORD-001", "status", "PENDING"));

        CheckoutConfirmResponse result = checkoutService.confirm(customerId, request);

        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.orderNumber()).isEqualTo("ORD-001");
        verify(cartFactory, times(1)).getOrCreateCart(customerId);
    }

    @Test
    void preview_delegatesToCartFactory() {
        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());
        item.setMedicineName("Panadol");
        item.setQty(2);
        item.setUnitPrice(BigDecimal.valueOf(25000));

        CheckoutPreviewRequest request = new CheckoutPreviewRequest(
                UUID.randomUUID(), "pickup", null);

        when(cartFactory.getOrCreateCart(customerId)).thenReturn(cart);
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(item));

        var result = checkoutService.preview(customerId, request);

        assertThat(result).isNotNull();
        verify(cartFactory, times(1)).getOrCreateCart(customerId);
    }

    @Test
    void confirm_withCodPayment_setsCartCheckedOut() {
        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());
        item.setMedicineId(UUID.randomUUID());
        item.setQty(1);
        item.setMedicineName("Panadol");
        item.setUnitPrice(BigDecimal.valueOf(50000));

        UUID orderId = UUID.randomUUID();
        CheckoutConfirmRequest request = new CheckoutConfirmRequest(
                UUID.randomUUID(), branchId, "pickup", null, "COD");

        when(cartFactory.getOrCreateCart(customerId)).thenReturn(cart);
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(item));
        when(orderClient.createOrder(any(Map.class)))
                .thenReturn(Map.of("id", orderId.toString(), "orderNumber", "ORD-002", "status", "PENDING"));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        checkoutService.confirm(customerId, request);

        assertThat(cart.getStatus()).isEqualTo(CartStatus.CHECKED_OUT);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void confirm_withEmptyCart_throwsInvalidOperationException() {
        CheckoutConfirmRequest request = new CheckoutConfirmRequest(
                UUID.randomUUID(), branchId, "pickup", null, "COD");

        when(cartFactory.getOrCreateCart(customerId)).thenReturn(cart);
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of());

        assertThatThrownBy(() -> checkoutService.confirm(customerId, request))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Cart is empty");
    }
}
