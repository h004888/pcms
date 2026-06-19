package com.pcms.customerportal.service;

import com.pcms.customerportal.dto.request.AddCartItemRequest;
import com.pcms.customerportal.dto.request.UpdateCartItemRequest;
import com.pcms.customerportal.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse getCart(UUID customerId);

    CartResponse addItem(UUID customerId, AddCartItemRequest request);

    CartResponse updateItem(UUID customerId, UUID itemId, UpdateCartItemRequest request);

    CartResponse removeItem(UUID customerId, UUID itemId);

    CartResponse clearCart(UUID customerId);
}
