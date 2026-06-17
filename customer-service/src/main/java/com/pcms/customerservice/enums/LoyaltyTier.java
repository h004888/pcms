package com.pcms.customerservice.enums;

/** B10: Loyalty tier auto-calculated from points balance. */
public enum LoyaltyTier {
    BRONZE,   // 0 – 999 points
    SILVER,   // 1000 – 4999 points
    GOLD;     // 5000+ points

    public static LoyaltyTier of(int points) {
        if (points >= 5000) return GOLD;
        if (points >= 1000) return SILVER;
        return BRONZE;
    }
}
