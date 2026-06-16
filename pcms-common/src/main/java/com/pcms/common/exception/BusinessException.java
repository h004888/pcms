package com.pcms.common.exception;

import com.pcms.common.dto.ErrorResponse;

/**
 * Base class for all business/domain exceptions thrown by PCMS services.
 * Each subclass carries:
 *   - a stable MSGxx code (from the 34-message catalog)
 *   - an HTTP status to return
 *   - English + Vietnamese messages (i18n-ready, CR-01)
 *
 * The GlobalExceptionHandler maps these to the standard ErrorResponse envelope.
 */
public abstract class BusinessException extends RuntimeException {

    private final String code;
    private final int httpStatus;
    private final String messageVi;

    protected BusinessException(String code, int httpStatus, String message, String messageVi) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
        this.messageVi = messageVi;
    }

    public String getCode() {
        return code;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getMessageVi() {
        return messageVi;
    }

    public ErrorResponse toErrorResponse() {
        return ErrorResponse.of(code, httpStatus, getMessage(), messageVi);
    }
}
