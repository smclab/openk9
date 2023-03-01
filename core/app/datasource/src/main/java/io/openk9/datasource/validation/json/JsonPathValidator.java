package io.openk9.datasource.validation.json;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@ApplicationScoped
public class JsonPathValidator implements ConstraintValidator<JsonPath, String> {
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {

		if (value == null) {
			return true;
		}

		try {
			com.jayway.jsonpath.JsonPath.compile(value);
			return true;
		}
		catch (Exception e) {
			context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
			return false;
		}
	}
}
