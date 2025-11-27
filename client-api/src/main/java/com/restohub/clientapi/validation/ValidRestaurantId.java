package com.restohub.clientapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidRestaurantIdValidator.class)
@Documented
public @interface ValidRestaurantId {
    String message() default "Restaurant not found or inactive";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

