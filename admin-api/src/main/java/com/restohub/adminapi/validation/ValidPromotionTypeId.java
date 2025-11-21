package com.restohub.adminapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPromotionTypeIdValidator.class)
@Documented
public @interface ValidPromotionTypeId {
    String message() default "Promotion type not found or inactive";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

