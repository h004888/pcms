package com.pcms.customerportal.enums;

/**
 * Lifecycle status of a home banner.
 * ACTIVE: visible to customers.
 * INACTIVE: hidden (admin disabled).
 * SCHEDULED: scheduled to display within [start_at, end_at].
 */
public enum BannerStatus {
    ACTIVE,
    INACTIVE,
    SCHEDULED,
    DELETED
}
