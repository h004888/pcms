package com.pcms.orderservice.saga;

/**
 * Type of distributed transaction orchestrated by the saga.
 * Each type has its own predefined step sequence.
 */
public enum SagaType {
    ORDER_FULFILLMENT   // Stock consume → points award → notification
}