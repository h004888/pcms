package com.pcms.customerportal.enums;

/**
 * Generic record status for soft-delete (CR-08).
 * <p>Used by CustomerAddress, CustomerFamily and other records that
 * are soft-deleted instead of physically removed.
 */
public enum RecordStatus {
    ACTIVE,
    INACTIVE
}
