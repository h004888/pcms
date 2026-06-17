package com.pcms.userservice.dto.request;

import static com.pcms.userservice.dto.request.ValidationMessages.EMAIL_TOO_LONG;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Composed validation for user email request fields. */
@Documented
@NotBlank
@Email
@Size(max = 100, message = EMAIL_TOO_LONG)
@Constraint(validatedBy = {})
@ReportAsSingleViolation
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserEmail {

    String message() default "Email không hợp lệ";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}