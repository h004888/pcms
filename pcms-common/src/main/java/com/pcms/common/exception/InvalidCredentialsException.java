package com.pcms.common.exception;

/**
 * Thrown when login credentials are wrong (email/password mismatch).
 * Maps to HTTP 401 + MSG01.
 */
public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("MSG01", 401,
                "Invalid email or password",
                "Email hoặc mật khẩu không đúng");
    }
}
