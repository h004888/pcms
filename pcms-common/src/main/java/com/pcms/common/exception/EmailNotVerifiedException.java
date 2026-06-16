package com.pcms.common.exception;

/**
 * Thrown when a user attempts to log in but their email is not verified.
 * Maps to HTTP 403 + MSG03.
 */
public class EmailNotVerifiedException extends BusinessException {

    public EmailNotVerifiedException() {
        super("MSG03", 403,
                "Email not verified",
                "Email chưa được xác thực");
    }
}
