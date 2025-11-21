package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = InstagramValidator.class)
@Documented
public @interface Instagram {
    String message() default "Invalid Instagram format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

