package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidRecurrenceTypeValidator.class)
@Documented
public @interface ValidRecurrenceType {
    String message() default "Invalid recurrence type. Must be WEEKLY, MONTHLY, or DAILY";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

