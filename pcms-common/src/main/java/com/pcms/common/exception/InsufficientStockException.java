package com.pcms.common.exception;

/**
 * Thrown when an order/cart requires more stock than is on hand.
 * Maps to HTTP 409 + MSG20.
 */
public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String medicineName, int requested, int available) {
        super("MSG20", 409,
                String.format("Insufficient stock for %s: requested %d, available %d", medicineName, requested, available),
                String.format("Không đủ tồn kho cho %s: yêu cầu %d, còn %d", medicineName, requested, available));
    }

    public InsufficientStockException(String message, String messageVi) {
        super("MSG20", 409, message, messageVi);
    }
}
