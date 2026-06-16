package com.pcms.customerservice.repository;

import com.pcms.customerservice.entity.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {

    /**
     * Lookup by reference order id (idempotency check for BR07 / NSF-04).
     * Returns empty if no transaction was ever recorded for this order.
     */
    Optional<LoyaltyTransaction> findByRefOrderId(UUID refOrderId);
}
