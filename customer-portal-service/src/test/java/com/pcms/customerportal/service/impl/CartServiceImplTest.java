package com.pcms.customerportal.service.impl;

import com.pcms.customerportal.client.CatalogClient;
import com.pcms.customerportal.dto.request.AddCartItemRequest;
import com.pcms.customerportal.dto.request.UpdateCartItemRequest;
import com.pcms.customerportal.dto.response.CartResponse;
import com.pcms.customerportal.entity.Cart;
import com.pcms.customerportal.entity.CartItem;
import com.pcms.customerportal.enums.CartStatus;
import com.pcms.customerportal.repository.CartItemRepository;
import com.pcms.customerportal.repository.CartRepository;
import com.pcms.customerportal.service.CartFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private CatalogClient catalogClient;
    @Mock private CartFactory cartFactory;
    @InjectMocks private CartServiceImpl cartService;

    private UUID customerId;
    private Cart cart;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setCustomerId(customerId);
        cart.setStatus(CartStatus.ACTIVE);
    }

    @Test
    void getCart_delegatesToCartFactory() {
        when(cartFactory.getOrCreateCart(customerId)).thenReturn(cart);
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of());

        CartResponse result = cartService.getCart(customerId);

        assertThat(result).isNotNull();
        verify(cartFactory, times(1)).getOrCreateCart(customerId);
    }

    @Test
    void addItem_delegatesToCartFactory() {
        UUID medicineId = UUID.randomUUID();
        AddCartItemRequest request = new AddCartItemRequest(medicineId, 2);
        Map<String, Object> medicine = Map.of("name", "Panadol", "price", 50000);
        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());
        item.setQty(1);
        item.setUnitPrice(BigDecimal.valueOf(50000));
        item.setCart(cart);

        when(cartFactory.getOrCreateCart(customerId)).thenReturn(cart);
        when(catalogClient.getById(medicineId.toString())).thenReturn(medicine);
        when(cartItemRepository.findByCartIdAndMedicineId(cart.getId(), medicineId))
                .thenReturn(Optional.of(item));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(item));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse result = cartService.addItem(customerId, request);

        assertThat(result).isNotNull();
        verify(cartFactory, times(1)).getOrCreateCart(customerId);
    }

    @Test
    void updateItem_delegatesToCartFactory() {
        UUID itemId = UUID.randomUUID();
        UpdateCartItemRequest request = new UpdateCartItemRequest(3);
        CartItem item = new CartItem();
        item.setId(itemId);
        item.setCart(cart);
        item.setUnitPrice(BigDecimal.valueOf(50000));

        when(cartFactory.getOrCreateCart(customerId)).thenReturn(cart);
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(item));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse result = cartService.updateItem(customerId, itemId, request);

        assertThat(result).isNotNull();
        verify(cartFactory, times(1)).getOrCreateCart(customerId);
    }

    @Test
    void removeItem_delegatesToCartFactory() {
        UUID itemId = UUID.randomUUID();
        CartItem item = new CartItem();
        item.setId(itemId);
        item.setCart(cart);

        when(cartFactory.getOrCreateCart(customerId)).thenReturn(cart);
        when(cartItemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse result = cartService.removeItem(customerId, itemId);

        assertThat(result).isNotNull();
        verify(cartFactory, times(1)).getOrCreateCart(customerId);
        verify(cartItemRepository, times(1)).delete(item);
    }

    @Test
    void clearCart_delegatesToCartFactory() {
        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartResponse result = cartService.clearCart(customerId);

        assertThat(result).isNotNull();
        verify(cartItemRepository, times(1)).deleteByCartId(cart.getId());
    }
}
