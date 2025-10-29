/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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

package io.openk9.common.util.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jakarta.validation.ConstraintViolation;

public class ResponseUtil {

	public static <T> List<FieldValidator> toFieldValidators(
		Set<ConstraintViolation<T>> violations) {

		List<FieldValidator> fieldValidators = new ArrayList<>();

		for (ConstraintViolation<T> violation : violations) {

			String field = violation.getPropertyPath().toString();
			String message = violation.getMessage();
			FieldValidator apply = FieldValidator.of(field, message);
			fieldValidators.add(apply);
		}

		return fieldValidators;
	}

}
