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

package io.openk9.datasource.model.util;

import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;

public class DocTypeFieldUtils {

	/**
	 * Builds the path of a field, joining the names of its parents with dots.
	 * The docType name is taken from the field itself.
	 *
	 * @param docTypeField the field whose path is built
	 * @return the path, for example {@code acl.roles.keyword}
	 * @throws IncompleteDocTypeFieldException if the field or one of its
	 * 	parents has no fieldName
	 */
	public static String fieldPath(DocTypeField docTypeField) {
		DocType docType = docTypeField.getDocType();
		return fieldPath(docType != null ? docType.getName() : null, docTypeField);
	}

	/**
	 * Builds the path of a field, joining the names of its parents with dots
	 * and using the given docType name as first segment. The {@code default}
	 * docType adds no segment.
	 *
	 * <p>The docType name is passed along the whole chain, so the path stays
	 * the same whatever parents have been loaded.
	 *
	 * @param docTypeName the name of the docType the field belongs to
	 * @param docTypeField the field whose path is built
	 * @return the path, for example {@code acl.roles.keyword}
	 * @throws IncompleteDocTypeFieldException if the field or one of its
	 * 	parents has no fieldName
	 */
	public static String fieldPath(String docTypeName, DocTypeField docTypeField) {

		String fieldName = docTypeField.getFieldName();

		if (fieldName == null) {
			throw new IncompleteDocTypeFieldException(String.format(
				"Cannot build the path: the docTypeField with id \"%s\" has no"
				+ " fieldName.",
				docTypeField.getId()
			));
		}

		DocTypeField parent = docTypeField.getParentDocTypeField();

		if (parent != null) {
			return fieldPath(docTypeName, parent) + "." + fieldName;
		}

		String rootPath =
			docTypeName != null && !docTypeName.equals(DocType.DEFAULT_NAME)
				? docTypeName + "."
				: "";

		return rootPath + fieldName;
	}
}
