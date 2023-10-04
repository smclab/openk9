package io.openk9.datasource.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy={AlnumValidator.class})
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Alnum {

	String message() default "is not an alphanumeric string";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
