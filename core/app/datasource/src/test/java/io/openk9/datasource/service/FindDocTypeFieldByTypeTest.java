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

package io.openk9.datasource.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FindDocTypeFieldByTypeTest {

	private static final String ENTITY_NAME_PREFIX = "FindDocTypeFieldByTypeTest - ";
	private static final String TEXT_FIELD_ONE_NAME = ENTITY_NAME_PREFIX + "Text field 1";
	private static final String TEXT_FIELD_TWO_NAME = ENTITY_NAME_PREFIX + "Text field 2";
	private static final String KEYWORD_FIELD_NAME = ENTITY_NAME_PREFIX + "Keyword field 1";
	private static final String BOOLEAN_FIELD_NAME = ENTITY_NAME_PREFIX + "Boolean field 1";

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	Mutiny.SessionFactory sf;

	@BeforeEach
	void setup() {
		createDocTypeField(TEXT_FIELD_ONE_NAME, FieldType.TEXT);
		createDocTypeField(TEXT_FIELD_TWO_NAME, FieldType.TEXT);
		createDocTypeField(KEYWORD_FIELD_NAME, FieldType.KEYWORD);
		createDocTypeField(BOOLEAN_FIELD_NAME, FieldType.BOOLEAN);
	}

	@Test
	@DisplayName("Should return all DocTypeFields matching the given FieldType")
	void should_return_fields_matching_type() {
		List<DocTypeField> textFields = docTypeFieldService.findDocTypeFieldByType(FieldType.TEXT)
			.await()
			.indefinitely();

		assertNotNull(textFields);
		assertFalse(textFields.isEmpty());

		assertTrue(
			textFields.stream().allMatch(f -> f.getFieldType() == FieldType.TEXT));

		List<String> names = textFields.stream()
			.map(DocTypeField::getName)
			.toList();

		assertTrue(names.contains(TEXT_FIELD_ONE_NAME));
		assertTrue(names.contains(TEXT_FIELD_TWO_NAME));
	}

	@Test
	@DisplayName("Should return only KEYWORD fields when filtering by KEYWORD")
	void should_return_only_keyword_fields() {
		List<DocTypeField> keywordFields =
			docTypeFieldService.findDocTypeFieldByType(FieldType.KEYWORD)
				.await()
				.indefinitely();

		assertNotNull(keywordFields);
		assertFalse(keywordFields.isEmpty());

		assertTrue(
			keywordFields.stream().allMatch(f -> f.getFieldType() == FieldType.KEYWORD));

		List<String> names = keywordFields.stream()
			.map(DocTypeField::getName)
			.toList();

		assertTrue(names.contains(KEYWORD_FIELD_NAME));
		assertFalse(names.contains(TEXT_FIELD_ONE_NAME));
	}

	@Test
	@DisplayName("Should return empty list when no DocTypeFields match the FieldType")
	void should_return_empty_list_for_unmatched_type() {
		List<DocTypeField> dateFields =
			docTypeFieldService.findDocTypeFieldByType(FieldType.DATE)
				.await()
				.indefinitely();

		assertNotNull(dateFields);
		assertTrue(dateFields.isEmpty());
	}

	@Test
	@DisplayName("Should not mix different FieldTypes in results")
	void should_not_mix_field_types() {
		List<DocTypeField> booleanFields =
			docTypeFieldService.findDocTypeFieldByType(FieldType.BOOLEAN)
				.await()
				.indefinitely();

		assertNotNull(booleanFields);
		assertFalse(booleanFields.isEmpty());

		assertTrue(
			booleanFields.stream().allMatch(f -> f.getFieldType() == FieldType.BOOLEAN));

		assertEquals(
			1,
			booleanFields.stream()
				.filter(f -> f.getName().startsWith(ENTITY_NAME_PREFIX))
				.count());
	}

	@AfterEach
	void tearDown() {
		EntitiesUtils.removeEntity(TEXT_FIELD_ONE_NAME, docTypeFieldService, sf);
		EntitiesUtils.removeEntity(TEXT_FIELD_TWO_NAME, docTypeFieldService, sf);
		EntitiesUtils.removeEntity(KEYWORD_FIELD_NAME, docTypeFieldService, sf);
		EntitiesUtils.removeEntity(BOOLEAN_FIELD_NAME, docTypeFieldService, sf);
	}

	private void createDocTypeField(String name, FieldType fieldType) {
		DocTypeFieldDTO dto = DocTypeFieldDTO.builder()
			.name(name)
			.fieldName(name)
			.searchable(false)
			.sortable(false)
			.fieldType(fieldType)
			.build();

		EntitiesUtils.createEntity(dto, docTypeFieldService, sf);
	}

}
