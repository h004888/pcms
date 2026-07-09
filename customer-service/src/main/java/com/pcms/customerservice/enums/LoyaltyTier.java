package com.pcms.customerservice.enums;

/**
 * B10: Loyalty tier auto-calculated from points balance.
 * 0–999 Bronze, 1000–4999 Silver, 5000+ Gold.
 */
public enum LoyaltyTier {
    BRONZE,
    SILVER,
    GOLD;

    public static LoyaltyTier fromPoints(Integer points) {
        int balance = points == null ? 0 : points;
        if (balance >= 5000) {
            return GOLD;
        }
        if (balance >= 1000) {
            return SILVER;
        }
        return BRONZE;
    }
}
