package com.pcms.common.exception;

import java.time.LocalDateTime;

/**
 * Thrown when an account is locked due to too many failed logins (BR05).
 * Maps to HTTP 423 (Locked) + MSG02.
 */
public class AccountLockedException extends BusinessException {

    private final LocalDateTime lockedUntil;

    public AccountLockedException(LocalDateTime lockedUntil) {
        super("MSG02", 423,
                "Account locked. Try again in 30 minutes",
                "Tài khoản đã bị khóa. Vui lòng thử lại sau 30 phút");
        this.lockedUntil = lockedUntil;
    }

    public AccountLockedException(String message, String messageVi, LocalDateTime lockedUntil) {
        super("MSG02", 423, message, messageVi);
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
}
