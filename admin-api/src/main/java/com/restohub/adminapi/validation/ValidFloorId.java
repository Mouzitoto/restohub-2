package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidFloorIdValidator.class)
@Documented
public @interface ValidFloorId {
    String message() default "Floor not found, inactive, or does not belong to restaurant";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

