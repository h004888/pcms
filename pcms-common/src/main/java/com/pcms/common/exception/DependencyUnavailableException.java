package com.pcms.common.exception;

/** Returned when a required internal service cannot be reached. */
public class DependencyUnavailableException extends BusinessException {

    public DependencyUnavailableException(String dependency, String messageVi) {
        super("MSG34", 503,
                dependency + " is temporarily unavailable",
                messageVi);
    }
}
