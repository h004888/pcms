package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.WalletTransaction;
import com.pcms.customerportal.enums.WalletTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    /** Paginated history for the wallet screen. */
    Page<WalletTransaction> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    /**
     * Sum of all amounts to compute the current balance.
     * Equivalent to: SELECT COALESCE(SUM(amount), 0) FROM wallet_transactions
     *                WHERE customer_id = :customerId.
     */
    @Query("SELECT COALESCE(SUM(w.amount), 0) FROM WalletTransaction w WHERE w.customerId = :customerId")
    int sumAmountByCustomer(@Param("customerId") UUID customerId);

    /** Sum of EARN-type only - used as an audit cross-check. */
    @Query("SELECT COALESCE(SUM(w.amount), 0) FROM WalletTransaction w " +
           "WHERE w.customerId = :customerId AND w.type = :type")
    int sumAmountByCustomerAndType(@Param("customerId") UUID customerId,
                                   @Param("type") WalletTransactionType type);
}
