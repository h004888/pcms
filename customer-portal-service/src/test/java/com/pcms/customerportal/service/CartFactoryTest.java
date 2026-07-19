package com.pcms.customerportal.service;

import com.pcms.customerportal.entity.Cart;
import com.pcms.customerportal.enums.CartStatus;
import com.pcms.customerportal.repository.CartItemRepository;
import com.pcms.customerportal.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartFactoryTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @InjectMocks private CartFactory cartFactory;

    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
    }

    @Test
    void getOrCreateCart_whenNoCart_createsNewActiveCart() {
        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(cartRepository.findByCustomerId(customerId))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartFactory.getOrCreateCart(customerId);

        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getStatus()).isEqualTo(CartStatus.ACTIVE);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void getOrCreateCart_whenOnlyCheckedOutCart_reactivatesIt() {
        Cart checkedOutCart = new Cart();
        checkedOutCart.setId(UUID.randomUUID());
        checkedOutCart.setCustomerId(customerId);
        checkedOutCart.setStatus(CartStatus.CHECKED_OUT);

        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(cartRepository.findByCustomerId(customerId))
                .thenReturn(Optional.of(checkedOutCart));
        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartFactory.getOrCreateCart(customerId);

        assertThat(result.getStatus()).isEqualTo(CartStatus.ACTIVE);
        verify(cartItemRepository, times(1)).deleteByCartId(checkedOutCart.getId());
        verify(cartRepository, times(1)).save(checkedOutCart);
    }

    @Test
    void getOrCreateCart_whenActiveCartExists_returnsIt() {
        Cart activeCart = new Cart();
        activeCart.setId(UUID.randomUUID());
        activeCart.setCustomerId(customerId);
        activeCart.setStatus(CartStatus.ACTIVE);

        when(cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));

        Cart result = cartFactory.getOrCreateCart(customerId);

        assertThat(result).isSameAs(activeCart);
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartRepository, never()).findByCustomerId(any(UUID.class));
    }
}
