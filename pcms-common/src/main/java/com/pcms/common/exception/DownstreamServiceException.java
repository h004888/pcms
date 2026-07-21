package com.pcms.common.exception;

public class DownstreamServiceException extends BusinessException {

    public DownstreamServiceException(String code, int httpStatus, String message, String messageVi) {
        super(code, httpStatus, message, messageVi);
    }
}
