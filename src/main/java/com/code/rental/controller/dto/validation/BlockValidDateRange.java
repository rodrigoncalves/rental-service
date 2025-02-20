package com.code.rental.controller.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = BlockDateRangeValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface BlockValidDateRange {
    String message() default "Start date must be before or the same day as end date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}