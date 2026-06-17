package com.pcms.customerservice.enums;

/**
 * Loyalty tier derived from current points balance.
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