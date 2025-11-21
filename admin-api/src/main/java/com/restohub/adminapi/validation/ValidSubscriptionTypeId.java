package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidSubscriptionTypeIdValidator.class)
@Documented
public @interface ValidSubscriptionTypeId {
    String message() default "Subscription type not found or inactive";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

