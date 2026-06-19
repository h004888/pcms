package com.pcms.customerportal.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.common.exception.BusinessException;
import com.pcms.customerportal.client.OrderClient;
import com.pcms.customerportal.dto.request.RedeemPointsRequest;
import com.pcms.customerportal.dto.response.RedeemResponse;
import com.pcms.customerportal.dto.response.WalletResponse;
import com.pcms.customerportal.dto.response.WalletResponse.WalletTierResponse;
import com.pcms.customerportal.dto.response.WalletTransactionResponse;
import com.pcms.customerportal.entity.WalletTier;
import com.pcms.customerportal.entity.WalletTransaction;
import com.pcms.customerportal.enums.WalletTransactionType;
import com.pcms.customerportal.repository.WalletTierRepository;
import com.pcms.customerportal.repository.WalletTransactionRepository;
import com.pcms.customerportal.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Wallet service implementation.
 * <p>Tier assignment: customer's lifetime spend (estimated via order-service)
 * is matched against {@code wallet_tiers} in DESC order of {@code minSpend}.
 * The first tier where {@code spend >= minSpend} is the current tier;
 * the next tier is the one with the smallest {@code minSpend > spend}.
 */
@Service
public class WalletServiceImpl implements WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final WalletTransactionRepository txRepo;
    private final WalletTierRepository tierRepo;
    private final OrderClient orderClient;

    public WalletServiceImpl(WalletTransactionRepository txRepo,
                             WalletTierRepository tierRepo,
                             OrderClient orderClient) {
        this.txRepo = txRepo;
        this.tierRepo = tierRepo;
        this.orderClient = orderClient;
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID currentCustomerId) {
        int balance = txRepo.sumAmountByCustomer(currentCustomerId);
        BigDecimal lifetimeSpend = orderClient.estimateLifetimeSpend(currentCustomerId.toString());

        List<WalletTier> tiersDesc = tierRepo.findAllByOrderByMinSpendDesc();
        WalletTier currentTier = null;
        WalletTier nextTier = null;
        for (WalletTier t : tiersDesc) {
            if (lifetimeSpend.compareTo(t.getMinSpend()) >= 0) {
                currentTier = t;
                break;
            }
        }
        if (currentTier == null && !tiersDesc.isEmpty()) {
            // Customer is below the lowest tier (no qualifying spend)
            currentTier = tiersDesc.get(tiersDesc.size() - 1);
        }
        if (currentTier != null) {
            Optional<WalletTier> next = tierRepo
                    .findFirstByMinSpendGreaterThanOrderByMinSpendAsc(currentTier.getMinSpend());
            nextTier = next.orElse(null);
        }

        int pointsToNext = 0;
        if (nextTier != null) {
            BigDecimal diff = nextTier.getMinSpend().subtract(lifetimeSpend);
            // Heuristic: 1,000 VND = 1 point (BR07)
            pointsToNext = Math.max(0, diff.divide(BigDecimal.valueOf(1_000L)).intValue());
        }

        // Most recent 5 transactions
        Page<WalletTransaction> recent = txRepo.findByCustomerIdOrderByCreatedAtDesc(
                currentCustomerId, PageRequest.of(0, 5));
        List<WalletTransactionResponse> recentTxs = recent.getContent().stream()
                .map(WalletTransactionResponse::from).toList();

        return new WalletResponse(
                currentCustomerId,
                balance,
                lifetimeSpend,
                currentTier == null ? null : WalletTierResponse.from(currentTier),
                nextTier == null ? null : WalletTierResponse.from(nextTier),
                pointsToNext,
                currentTier == null ? List.of() : parsePerks(currentTier.getPerks()),
                recentTxs
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WalletTransactionResponse> getTransactions(UUID currentCustomerId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Page<WalletTransaction> p = txRepo.findByCustomerIdOrderByCreatedAtDesc(
                currentCustomerId, PageRequest.of(safePage, safeSize));
        return PageResponse.of(p, WalletTransactionResponse::from);
    }

    @Override
    @Transactional
    public RedeemResponse redeem(UUID currentCustomerId, RedeemPointsRequest req) {
        int currentBalance = txRepo.sumAmountByCustomer(currentCustomerId);
        if (currentBalance < req.points()) {
            throw new BusinessException(
                    "MSG33", 400,
                    "Insufficient wallet balance: requested " + req.points()
                            + " points, have " + currentBalance,
                    "Số dư ví không đủ: yêu cầu " + req.points()
                            + " điểm, hiện có " + currentBalance);
        }

        int newBalance = currentBalance - req.points();
        WalletTransaction tx = new WalletTransaction();
        tx.setCustomerId(currentCustomerId);
        tx.setType(WalletTransactionType.REDEEM);
        tx.setAmount(-req.points());
        tx.setBalanceAfter(newBalance);
        tx.setSource("REDEMPTION");
        tx.setNote("Redeemed for reward type: " + req.rewardType());
        WalletTransaction saved = txRepo.save(tx);

        String voucherCode = "RW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        log.info("[wallet] customer={} redeemed {} points (new balance={}) voucher={}",
                currentCustomerId, req.points(), newBalance, voucherCode);

        return new RedeemResponse(
                "REDEEMED",
                req.points(),
                newBalance,
                voucherCode,
                saved.getId(),
                "Points redeemed successfully. Voucher code: " + voucherCode);
    }

    // ====================================================================
    // Helpers
    // ====================================================================

    private List<String> parsePerks(String perksJson) {
        if (perksJson == null || perksJson.isBlank()) return List.of();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(perksJson, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
