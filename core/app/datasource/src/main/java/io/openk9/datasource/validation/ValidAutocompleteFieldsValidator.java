/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.validation;

import java.util.Set;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import io.openk9.datasource.model.DocTypeField;

public class ValidAutocompleteFieldsValidator
	implements ConstraintValidator<ValidAutocompleteFields, Set<DocTypeField>> {
	@Override
	public boolean isValid(Set<DocTypeField> fields, ConstraintValidatorContext context) {
		return validateAutocompleteFields(fields);
	}

	public static boolean validateAutocompleteFields(Set<DocTypeField> fields) {
		if (fields == null || fields.isEmpty()) {
			return true;
		}

		return fields.stream()
			.allMatch(field ->
				field != null
					&& field.isAutocomplete()
					&& field.getParentDocTypeField() != null
			);
	}
}
