package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidMenuCategoryIdValidator.class)
@Documented
public @interface ValidMenuCategoryId {
    String message() default "Menu category not found or inactive";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

