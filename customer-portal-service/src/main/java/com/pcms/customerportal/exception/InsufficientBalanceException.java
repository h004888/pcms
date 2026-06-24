package com.pcms.customerportal.exception;

import com.pcms.common.exception.BusinessException;

public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException(String message, String messageVi) {
        super("MSG33", 400, message, messageVi);
    }
}