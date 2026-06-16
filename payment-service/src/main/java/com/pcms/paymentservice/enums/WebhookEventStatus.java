package com.pcms.paymentservice.enums;

public enum WebhookEventStatus {
    RECEIVED,     // received from gateway, not yet processed
    PROCESSED,    // successfully processed
    DUPLICATE,    // already processed (idempotent skip)
    FAILED        // processing failed
}
