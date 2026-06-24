package com.pcms.common.exception;

import com.pcms.common.correlation.CorrelationContext;
import com.pcms.common.correlation.CorrelationIdFilter;
import com.pcms.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Centralized exception handler for all PCMS services.
 * - Maps known Spring/JPA/Security exceptions to MSGxx codes
 * - Returns the standard ErrorResponse envelope (RFC 7807-inspired, see SRS v1.3.0 §6.16)
 * - Logs at WARN for business exceptions, ERROR for unknown ones (with stack trace)
 *
 * To use: add pcms-common as a dependency and ensure component scan covers
 * {@code com.pcms.common} (e.g. {@code @SpringBootApplication(scanBasePackages = "com.pcms")}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ====================================================================
    // 1) Business / domain exceptions (custom)
    // ====================================================================
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {
        log.warn("[{}] {} at {} -> {} (vi: {})",
                ex.getCode(), ex.getClass().getSimpleName(), req.getRequestURI(),
                ex.getMessage(), ex.getMessageVi());
        ErrorResponse body = withPath(ex.toErrorResponse(), req.getRequestURI());
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    // ====================================================================
    // 2) Validation
    // ====================================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex,
                                                              HttpServletRequest req) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(),
                    fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid");
        }
        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", fieldErrors);

        ErrorResponse body = new ErrorResponse(
                "MSG33", 400,
                "Invalid input data", "Dữ liệu không hợp lệ",
                details, req.getRequestURI(), null, Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex,
                                                          HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(withPath(ErrorResponse.of("MSG33", 400, ex.getMessage(), "Dữ liệu không hợp lệ"),
                        req.getRequestURI()));
    }

    // ====================================================================
    // 3) Not found / bad request
    // ====================================================================
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElement(NoSuchElementException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(withPath(ErrorResponse.of("MSG31", 404,
                                ex.getMessage() != null ? ex.getMessage() : "Resource not found",
                                "Không tìm thấy tài nguyên"),
                        req.getRequestURI()));
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(withPath(ErrorResponse.of("MSG33", 400, ex.getMessage(), "Dữ liệu không hợp lệ"),
                        req.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(
            org.springframework.web.bind.MissingRequestHeaderException ex, HttpServletRequest req) {
        log.debug("Missing required header '{}' at {}", ex.getHeaderName(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(withPath(ErrorResponse.of("MSG01", 401,
                                "Missing required header: " + ex.getHeaderName(),
                                "Thiếu header yêu cầu: " + ex.getHeaderName()),
                        req.getRequestURI()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(withPath(ErrorResponse.of("MSG33", 409, ex.getMessage(), "Trạng thái không hợp lệ"),
                        req.getRequestURI()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(withPath(ErrorResponse.of("MSG31", 404, "Endpoint not found: " + req.getRequestURI(),
                                "Không tìm thấy endpoint: " + req.getRequestURI()),
                        req.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(
            org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(withPath(ErrorResponse.of("MSG31", 404, "Endpoint not found: " + req.getRequestURI(),
                                "Không tìm thấy endpoint: " + req.getRequestURI()),
                        req.getRequestURI()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(withPath(ErrorResponse.of("MSG33", 405, ex.getMessage(), "Phương thức không được hỗ trợ"),
                        req.getRequestURI()));
    }

    // ====================================================================
    // 4) Database
    // ====================================================================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                              HttpServletRequest req) {
        Throwable root = ex.getMostSpecificCause();
        log.warn("Data integrity violation at {} -> {}", req.getRequestURI(),
                root != null ? root.getMessage() : ex.getMessage());
        String msg = root != null && root.getMessage() != null
                ? "Data integrity violation: " + root.getMessage()
                : "Data integrity violation";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(withPath(ErrorResponse.of("MSG09", 409, msg, "Vi phạm ràng buộc dữ liệu"),
                        req.getRequestURI()));
    }

    // ====================================================================
    // 5) Security
    // ====================================================================
    @ExceptionHandler({AuthenticationException.class, AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(withPath(ErrorResponse.of("MSG01", 401, "Authentication required", "Yêu cầu xác thực"),
                        req.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(withPath(ErrorResponse.of("MSG31", 403, "Access denied", "Không có quyền truy cập"),
                        req.getRequestURI()));
    }

    // ====================================================================
    // 6) Fallback
    // ====================================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}", req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(withPath(ErrorResponse.of("MSG34", 500, "Internal server error", "Lỗi máy chủ nội bộ"),
                        req.getRequestURI()));
    }

    // ====================================================================
    // Helpers
    // ====================================================================
    /**
     * Attach request path + correlation id to an existing ErrorResponse (preferred overload).
     */
    private ErrorResponse withPath(ErrorResponse r, HttpServletRequest req) {
        return new ErrorResponse(r.code(), r.status(), r.message(), r.messageVi(),
                r.details(), req.getRequestURI(),
                extractCorrelationId(req),
                r.timestamp());
    }

    /**
     * Backward-compatible overload — only fills the path; correlation id left null.
     */
    private ErrorResponse withPath(ErrorResponse r, String path) {
        return new ErrorResponse(r.code(), r.status(), r.message(), r.messageVi(),
                r.details(), path, null, r.timestamp());
    }

    /**
     * Extract correlation id from request attribute (set by CorrelationIdFilter) or header.
     * Fallback to MDC.
     */
    private String extractCorrelationId(HttpServletRequest req) {
        Object attr = req.getAttribute(CorrelationIdFilter.REQUEST_ATTR);
        if (attr != null) {
            return attr.toString();
        }
        String header = req.getHeader(CorrelationContext.HEADER);
        if (header != null && !header.isBlank()) {
            return header;
        }
        return org.slf4j.MDC.get(CorrelationContext.MDC_KEY);
    }
}
