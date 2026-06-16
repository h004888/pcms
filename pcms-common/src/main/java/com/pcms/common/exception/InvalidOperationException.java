package com.pcms.common.exception;

/**
 * Thrown when a request is structurally valid but conflicts with business rules
 * (e.g. cancelling a paid order, expiring a non-pending prescription).
 * Maps to HTTP 400 + MSG33 (or 409 + MSG33 depending on context).
 */
public class InvalidOperationException extends BusinessException {

    public InvalidOperationException(String message, String messageVi) {
        super("MSG33", 400, message, messageVi);
    }

    public InvalidOperationException(String message) {
        this(message, message);
    }

    public InvalidOperationException(String message, String messageVi, int httpStatus) {
        super("MSG33", httpStatus, message, messageVi);
    }
}
