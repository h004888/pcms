package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.WalletTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletTierRepository extends JpaRepository<WalletTier, UUID> {

    /** Tiers ordered from highest to lowest min spend; used to find the
     *  current tier given a customer's total spend. */
    List<WalletTier> findAllByOrderByMinSpendDesc();

    /** The next tier above the one the customer is currently in. */
    Optional<WalletTier> findFirstByMinSpendGreaterThanOrderByMinSpendAsc(BigDecimal minSpend);

    Optional<WalletTier> findByName(String name);
}
