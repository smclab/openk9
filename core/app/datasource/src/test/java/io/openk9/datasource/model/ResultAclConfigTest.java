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

package io.openk9.datasource.model;

import java.util.List;
import java.util.Set;

import io.openk9.datasource.model.ResultAclConfig.FieldNode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultAclConfigTest {

	// title (TEXT) -> title.keyword (KEYWORD)
	private static final FieldNode TITLE =
		new FieldNode(1L, null, FieldType.TEXT, null);
	private static final FieldNode TITLE_KEYWORD =
		new FieldNode(2L, 1L, FieldType.KEYWORD, null);

	// resources (OBJECT) -> binaries (NESTED) -> name (TEXT), data (BINARY)
	private static final FieldNode RESOURCES =
		new FieldNode(3L, null, FieldType.OBJECT, null);
	private static final FieldNode BINARIES =
		new FieldNode(4L, 3L, FieldType.NESTED, null);
	private static final FieldNode BINARY_NAME =
		new FieldNode(5L, 4L, FieldType.TEXT, null);
	private static final FieldNode BINARY_DATA =
		new FieldNode(6L, 4L, FieldType.BINARY, null);

	// content (I18N) -> i18n (OBJECT) -> it (TEXT), and base (TEXT)
	private static final FieldNode CONTENT =
		new FieldNode(7L, null, FieldType.I18N, null);
	private static final FieldNode CONTENT_I18N =
		new FieldNode(8L, 7L, FieldType.OBJECT, null);
	private static final FieldNode CONTENT_IT =
		new FieldNode(9L, 8L, FieldType.TEXT, null);
	private static final FieldNode CONTENT_BASE =
		new FieldNode(10L, 7L, FieldType.TEXT, null);

	// fields holding their whole value: an OBJECT without sub-fields and a
	// FLATTENED field
	private static final FieldNode RAW =
		new FieldNode(11L, null, FieldType.OBJECT, null);
	private static final FieldNode METADATA =
		new FieldNode(12L, null, FieldType.FLATTENED, null);

	// an excluded leaf, and a leaf with the flag explicitly false
	private static final FieldNode BODY =
		new FieldNode(13L, null, FieldType.TEXT, true);
	private static final FieldNode DATE =
		new FieldNode(14L, null, FieldType.DATE, false);

	@Test
	void should_select_the_most_specific_fields_holding_a_value() {
		// setup — every field of the tenant, so that parents and sub-fields
		// can be told apart
		var fields = List.of(
			TITLE, TITLE_KEYWORD,
			RESOURCES, BINARIES, BINARY_NAME, BINARY_DATA,
			CONTENT, CONTENT_I18N, CONTENT_IT, CONTENT_BASE,
			RAW, METADATA,
			BODY, DATE
		);

		// action
		var selectable = ResultAclConfig.selectableFieldIds(fields);

		// assertion — left out: the multi-field, the containers with
		// sub-fields, the variants of the i18n field and the excluded field
		assertEquals(
			Set.of(
				TITLE.id(),
				BINARY_NAME.id(), BINARY_DATA.id(),
				CONTENT.id(),
				RAW.id(), METADATA.id(),
				DATE.id()
			),
			selectable);
	}

}
