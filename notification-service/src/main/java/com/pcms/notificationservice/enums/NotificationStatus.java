package com.pcms.notificationservice.enums;

public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    READ,
    /** TICKET-304: soft-deleted by the recipient. Excluded from list queries. */
    DELETED
}
