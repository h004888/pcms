package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByCartId(UUID cartId);

    Optional<CartItem> findByCartIdAndMedicineId(UUID cartId, UUID medicineId);

    void deleteByCartId(UUID cartId);
}
