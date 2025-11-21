package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueBookingStatusCodeValidator.class)
@Documented
public @interface UniqueBookingStatusCode {
    String message() default "Booking status code already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

