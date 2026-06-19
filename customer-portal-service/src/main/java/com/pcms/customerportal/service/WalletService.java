package com.pcms.customerportal.service;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.dto.request.RedeemPointsRequest;
import com.pcms.customerportal.dto.response.RedeemResponse;
import com.pcms.customerportal.dto.response.WalletResponse;
import com.pcms.customerportal.dto.response.WalletTransactionResponse;

import java.util.UUID;

/**
 * TICKET-703 - Health Wallet service.
 * FR14.23 - Ví Khỏe Nhà Ta.
 *
 * <p>Authorization: every operation is scoped to the calling customer
 * (resolved from JWT). The wallet is a per-customer ledger; we never
 * let one customer read another's transactions.
 */
public interface WalletService {

    /** Full wallet view (balance + tier + recent transactions). */
    WalletResponse getWallet(UUID currentCustomerId);

    /** Paginated transaction history. */
    PageResponse<WalletTransactionResponse> getTransactions(UUID currentCustomerId, int page, int size);

    /** Redeem points for a reward (transactional: check balance, deduct, log). */
    RedeemResponse redeem(UUID currentCustomerId, RedeemPointsRequest request);
}
