package com.restohub.clientapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTableIdValidator.class)
@Documented
public @interface ValidTableId {
    String message() default "Table not found, inactive, or does not belong to restaurant";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    long restaurantId() default 0;
}

