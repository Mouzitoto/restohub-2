package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueCategoryNameValidator.class)
@Documented
public @interface UniqueCategoryName {
    String message() default "Category name already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

