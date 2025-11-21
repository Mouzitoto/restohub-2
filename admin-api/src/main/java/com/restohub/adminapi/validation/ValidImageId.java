package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidImageIdValidator.class)
@Documented
public @interface ValidImageId {
    String message() default "Image not found or inactive";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

