package com.pcms.orderservice.enums;

public enum OrderStatus {
    PENDING_PAYMENT,   // FR6.1 - newly placed
    PAID,              // FR6.2 - after payment success
    COMPLETED,         // after invoice printed/delivered
    CANCELLED          // BR01 auto-cancel or manual
}
