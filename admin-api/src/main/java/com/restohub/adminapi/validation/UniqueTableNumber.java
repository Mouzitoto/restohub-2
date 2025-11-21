package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueTableNumberValidator.class)
@Documented
public @interface UniqueTableNumber {
    String message() default "Table number already exists for this room";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

