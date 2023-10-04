package io.openk9.datasource.validation;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@ApplicationScoped
public class AlnumValidator implements ConstraintValidator<Alnum, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return ALNUM_PATTERN.test(value);
	}

	private final static Predicate<String> ALNUM_PATTERN = Pattern
		.compile("^[A-Za-z][A-Za-z0-9]*$")
		.asMatchPredicate();

}
