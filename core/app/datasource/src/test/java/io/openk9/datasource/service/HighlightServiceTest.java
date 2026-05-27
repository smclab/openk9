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

package io.openk9.datasource.service;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.Highlight;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.base.HighlightDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

@QuarkusTest
public class HighlightServiceTest {

	@Inject
	HighlightService highlightService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	private static final String DOC_TYPE_FIELD_NAME = "docTypeFieldTEST";

	@BeforeEach
	void setup() {
		createDocTypeField();
	}

	@Test
	void should_create_highlight() {
		var docTypeField = EntitiesUtils.getEntity(
			DOC_TYPE_FIELD_NAME,
			docTypeFieldService,
			sessionFactory
		);

		var highlightDTO = HighlightDTO.builder()
			.name("highlight")
			.type(Highlight.HighlightType.UNIFIED)
			.fieldIds(Set.of(docTypeField.getId()))
			.build();

		Assertions.assertDoesNotThrow(() -> highlightService.create(highlightDTO));
	}

	@AfterEach
	void tearDown() {
		deleteDocTypeField();
	}

	private void createDocTypeField() {
		var docTypeFieldDTO = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME)
			.fieldName("fieldName1")
			.searchable(true)
			.sortable(true)
			.fieldType(FieldType.BINARY)
			.build();

		EntitiesUtils.createEntity(
			docTypeFieldDTO,
			docTypeFieldService,
			sessionFactory
		);
	}

	private void deleteDocTypeField() {
		EntitiesUtils.removeEntity(
			DOC_TYPE_FIELD_NAME,
			docTypeFieldService,
			sessionFactory
		);
	}
}
