package com.pcms.customerportal.security;

import com.pcms.common.exception.BusinessException;

/**
 * Thrown when a B2C endpoint requires an authenticated customer
 * (X-User-Id) but the header is missing or invalid. Maps to 401.
 */
public class MissingUserHeaderException extends BusinessException {
    public MissingUserHeaderException(String message, String messageVi) {
        super("MSG01", 401, message, messageVi);
    }
}
