package com.pcms.customerportal.enums;

/**
 * Wallet transaction type per FR14.23 (Ví Khỏe Nhà Ta).
 * <ul>
 *   <li>EARN    - customer earns points (e.g. from paid order, FR14.23)</li>
 *   <li>REDEEM  - customer redeems points for a reward</li>
 *   <li>EXPIRE  - points expired after validity window (NSF)</li>
 *   <li>ADJUST  - manual adjustment by admin/customer service</li>
 * </ul>
 */
public enum WalletTransactionType {
    EARN,
    REDEEM,
    EXPIRE,
    ADJUST
}
