package com.pcms.userservice.dto.request;

import static com.pcms.userservice.dto.request.ValidationMessages.NEW_PASSWORD_SIZE;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Composed validation for password reset password fields. */
@Documented
@NotBlank
@Size(min = 8, max = 72, message = NEW_PASSWORD_SIZE)
@Constraint(validatedBy = {})
@ReportAsSingleViolation
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidResetPassword {

    String message() default "Mật khẩu mới không hợp lệ";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}