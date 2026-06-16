package com.pcms.common.exception;

/**
 * Thrown when a requested resource does not exist.
 * Maps to HTTP 404 + MSG31 (generic not-found).
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super("MSG31", 404,
                String.format("%s not found: %s", resource, id),
                String.format("Không tìm thấy %s: %s", resource, id));
    }

    public ResourceNotFoundException(String message, String messageVi) {
        super("MSG31", 404, message, messageVi);
    }
}
