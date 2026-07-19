package com.pcms.customerportal.service;

import com.pcms.customerportal.entity.Cart;
import com.pcms.customerportal.enums.CartStatus;
import com.pcms.customerportal.repository.CartItemRepository;
import com.pcms.customerportal.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CartFactory {

    private static final Logger log = LoggerFactory.getLogger(CartFactory.class);
    private static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("30000");

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartFactory(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Cart getOrCreateCart(UUID customerId) {
        return cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .or(() -> cartRepository.findByCustomerId(customerId).map(cart -> {
                    cart.setStatus(CartStatus.ACTIVE);
                    cartItemRepository.deleteByCartId(cart.getId());
                    cart.getItems().clear();
                    cart.setSubtotal(BigDecimal.ZERO);
                    cart.setDiscount(BigDecimal.ZERO);
                    cart.setTotal(BigDecimal.ZERO);
                    cart.setVoucherCode(null);
                    return cartRepository.save(cart);
                }))
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomerId(customerId);
                    newCart.setStatus(CartStatus.ACTIVE);
                    newCart.setShippingFee(DEFAULT_SHIPPING_FEE);
                    return cartRepository.save(newCart);
                });
    }
}
