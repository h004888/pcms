package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.Cart;
import com.pcms.customerportal.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByCustomerIdAndStatus(UUID customerId, CartStatus status);

    Optional<Cart> findByCustomerId(UUID customerId);
}
