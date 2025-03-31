package io.openk9.datasource.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.quartz.CronExpression;

public class ValidQuartzCronValidator implements ConstraintValidator<ValidQuartzCron, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return CronExpression.isValidExpression(value);
	}
}
