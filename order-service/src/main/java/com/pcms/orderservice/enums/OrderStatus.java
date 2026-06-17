package com.pcms.orderservice.enums;

public enum OrderStatus {
    PENDING_PAYMENT, // FR6.1 - newly placed
    APPROVED, // FR6.5 - manager approved before fulfillment/payment finalization
    PAID, // FR6.2 - after payment success
    COMPLETED, // after invoice printed/delivered
    REJECTED, // FR6.5 - manager rejected
    CANCELLED // BR01 auto-cancel or manual
}
