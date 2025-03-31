package io.openk9.datasource.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {ValidQuartzCronValidator.class})
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidQuartzCron {

	String message() default "is not a valid cron expression";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
