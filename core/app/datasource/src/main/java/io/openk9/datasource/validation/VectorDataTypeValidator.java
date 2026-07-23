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

package io.openk9.datasource.validation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.dto.base.EmbeddingModelDTO;

@ApplicationScoped
public class VectorDataTypeValidator
	implements ConstraintValidator<ValidVectorDataType, EmbeddingModelDTO> {

	@Override
	public boolean isValid(
		EmbeddingModelDTO value, ConstraintValidatorContext context) {

		if (value == null
			|| value.getVectorDataType() != EmbeddingModel.VectorDataType.BINARY) {

			return true;
		}

		if (value.getVectorSize() % 8 == 0) {
			return true;
		}

		// point the violation at vectorSize instead of the whole bean,
		// so the client sees which field to fix.
		context.disableDefaultConstraintViolation();
		context
			.buildConstraintViolationWithTemplate(
				context.getDefaultConstraintMessageTemplate())
			.addPropertyNode("vectorSize")
			.addConstraintViolation();

		return false;
	}

}
