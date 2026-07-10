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

import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDocTypeFieldOffsetSourceValidator
	implements ConstraintValidator<ValidDocTypeFieldOffsetSource, DocTypeField> {
	@Override
	public boolean isValid(DocTypeField docTypeField, ConstraintValidatorContext context) {
		if (docTypeField == null) {
			return true;
		}
		return docTypeField.getFieldType() == FieldType.TEXT
			|| docTypeField.getOffsetSource() == DocTypeField.OffsetSourceType.NONE;
	}
}
