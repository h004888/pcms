package com.pcms.common.exception;

/**
 * Thrown when an account is INACTIVE (soft-deleted per CR-08).
 * Maps to HTTP 403 + MSG31.
 */
public class InactiveAccountException extends BusinessException {

    public InactiveAccountException() {
        super("MSG31", 403,
                "Account is inactive",
                "Tài khoản không hoạt động");
    }
}
