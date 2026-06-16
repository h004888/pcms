package com.pcms.common.exception;

/**
 * Thrown when creating/updating a resource that would violate a uniqueness constraint.
 * Maps to HTTP 409 + MSG09 (duplicate).
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String field, Object value) {
        super("MSG09", 409,
                String.format("%s already exists: %s", field, value),
                String.format("%s đã tồn tại: %s", field, value));
    }

    public DuplicateResourceException(String message, String messageVi) {
        super("MSG09", 409, message, messageVi);
    }
}
