package com.pcms.inventoryservice.dto.response;

/**
 * Result for stock operations that don't return a Batch (export/consume/transfer).
 * Carries the message + the number of units moved.
 */
public record StockOperationResult(String message, int qty) {
    public static StockOperationResult of(String message, int qty) {
        return new StockOperationResult(message, qty);
    }
}
