package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidRoomIdValidator.class)
@Documented
public @interface ValidRoomId {
    String message() default "Room not found, inactive, or does not belong to restaurant";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

