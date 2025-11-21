package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueFloorNumberValidator.class)
@Documented
public @interface UniqueFloorNumber {
    String message() default "Floor number already exists for this restaurant";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

