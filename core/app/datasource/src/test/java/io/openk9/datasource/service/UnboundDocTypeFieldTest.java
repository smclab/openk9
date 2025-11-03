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

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.base.AutocorrectionDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.request.SuggestionCategoryWithDocTypeFieldDTO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnboundDocTypeFieldTest {

	private static final String ENTITY_NAME_PREFIX = "UnboundDocTypeFieldTest - ";
	private static final String DOC_TYPE_FIELD_ONE_NAME = ENTITY_NAME_PREFIX + "Doc type field 1";
	private static final String DOC_TYPE_FIELD_TWO_NAME = ENTITY_NAME_PREFIX + "Doc type field 2";
	private static final String DOC_TYPE_FIELD_THREE_NAME = ENTITY_NAME_PREFIX + "Doc type field 3";
	private static final String DOC_TYPE_FIELD_FOUR_NAME = ENTITY_NAME_PREFIX + "Doc type field 4";
	private static final String SUGGESTION_CATEGORY_ONE_NAME = ENTITY_NAME_PREFIX + "Suggestion category 1";
	private static final String AUTOCORRECTION_ONE_NAME = ENTITY_NAME_PREFIX + "Autocorrection 1";

	@Inject
	AutocorrectionService autocorrectionService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		//1. docTypeField with "KEYWORD" to bound to the suggestionCategoryOne
		createDocTypeFieldOne();
		//2. docTypeField with "KEYWORD" to leave unbound
		createDocTypeFieldTwo();
		//3. docTypeField with "I18N" to leave unbound
		createDocTypeFieldThree();
		//4. docTypeField without either "KEYWORD" or "I18N" to leave unbound
		createDocTypeFieldFour();

		//create suggestion category and bound with docTypeFieldOne
		createSuggestionCategoryOneWithDocTypeFieldOne();

		//create Autocorrection and bound with docTypeFieldOne
		AutocorrectionDTO dtoOne = AutocorrectionDTO.builder()
			.name(AUTOCORRECTION_ONE_NAME)
			.autocorrectionDocTypeFieldId(getDocTypeFieldOne().getId())
			.build();

		EntitiesUtils.createEntity(dtoOne, autocorrectionService, sessionFactory);
	}

	// Unbound by SuggestionCategory
	@Test
	void should_retrieve_all_filtered_unbound_doc_type_fields() {
		var actualUnboundDocTypeFields = getUnboundDocTypeFieldForSuggestionCategoryOne();
		var boundDocTypeFieldId = getSuggestionCategoryOne().getDocTypeField().getId();

		// Expected unbound DocTypeField are all filtered DocTypeField
		// except the one bound to the given SuggestionCategory.
		var expectedUnboundDocTypeFields = getAllFilteredDocTypeFields().stream()
			.filter(docTypeField -> !Objects.equals(docTypeField.getId(), boundDocTypeFieldId))
			.toList();

		assertFalse(actualUnboundDocTypeFields.isEmpty());
		assertEquals(expectedUnboundDocTypeFields.size(), actualUnboundDocTypeFields.size());
		assertEquals(expectedUnboundDocTypeFields, actualUnboundDocTypeFields);

	}

	@Test
	void should_retrieve_all_filtered_doc_type_fields_from_unbound_suggestion_category() {
		unsetDocTypeFieldFromSuggestionCategoryOne();
		var suggestionCategory = getSuggestionCategoryOne();

		assertNull(suggestionCategory.getDocTypeField());

		var actualUnboundDocTypeFields = getUnboundDocTypeFieldForSuggestionCategoryOne();

		// Expected unbound DocTypeField are all filtered DocTypeField
		var expectedUnboundDocTypeFields = getAllFilteredDocTypeFields();

		assertFalse(actualUnboundDocTypeFields.isEmpty());
		assertEquals(expectedUnboundDocTypeFields.size(), actualUnboundDocTypeFields.size());
		assertTrue(
			actualUnboundDocTypeFields.containsAll(expectedUnboundDocTypeFields));
		assertEquals(expectedUnboundDocTypeFields, actualUnboundDocTypeFields);

	}

	@Test
	void should_retrieve_all_filtered_doc_type_fields_from_missing_suggestion_category() {
		var actualUnboundDocTypeFields = getUnboundDocTypeFieldForMissingSuggestionCategory();

		// Expected unbound DocTypeField are all filtered DocTypeField
		var expectedUnboundDocTypeFields = getAllFilteredDocTypeFields();

		assertFalse(actualUnboundDocTypeFields.isEmpty());
		assertEquals(expectedUnboundDocTypeFields, actualUnboundDocTypeFields);
	}

	// Unbound by Autocorrection
	@Test
	void should_retrieve_all_textual_unbound_doc_type_fields() {
		var autocorrectionOne =
			EntitiesUtils.getEntity(AUTOCORRECTION_ONE_NAME, autocorrectionService, sessionFactory);

		var actualUnboundDocTypeFields =
			sessionFactory.withTransaction(s ->
					docTypeFieldService.findUnboundDocTypeFieldByAutocorrection(
						autocorrectionOne.getId()
					)
				)
				.await()
				.indefinitely();

		var boundDocTypeField = autocorrectionOne.getAutocorrectionDocTypeField();

		assertNotNull(boundDocTypeField);
		var boundDocTypeFieldId = boundDocTypeField.getId();

		// Expected unbound DocTypeField are all textual DocTypeField
		// except the one bound to the given Autocorrection.
		var expectedUnboundDocTypeFields = getAllTextDocTypeFields().stream()
			.filter(docTypeField -> !Objects.equals(docTypeField.getId(), boundDocTypeFieldId))
			.toList();

		assertFalse(actualUnboundDocTypeFields.isEmpty());
		assertEquals(expectedUnboundDocTypeFields.size(), actualUnboundDocTypeFields.size());
		assertEquals(expectedUnboundDocTypeFields, actualUnboundDocTypeFields);

	}

	@Test
	void should_retrieve_all_textual_doc_type_fields_from_missing_autocorrection() {
		var actualUnboundDocTypeFields = sessionFactory.withTransaction(s ->
			docTypeFieldService.findUnboundDocTypeFieldByAutocorrection(0L)
		)
		.await()
		.indefinitely();

		// Expected unbound DocTypeField are all filtered DocTypeField
		var expectedUnboundDocTypeFields = getAllTextDocTypeFields();

		assertFalse(actualUnboundDocTypeFields.isEmpty());
		assertEquals(expectedUnboundDocTypeFields, actualUnboundDocTypeFields);
	}

	@AfterEach
	void tearDown() {
		//remove suggestion
		removeSuggestionCategory(getSuggestionCategoryOne().getId());

		//remove all the four doc type fields
		removeDocTypeField(getDocTypeFieldOne().getId());
		removeDocTypeField(getDocTypeFieldTwo().getId());
		removeDocTypeField(getDocTypeFieldThree().getId());
		removeDocTypeField(getDocTypeFieldFour().getId());

		EntitiesUtils.removeEntity(AUTOCORRECTION_ONE_NAME, autocorrectionService, sessionFactory);
	}

	private void createDocTypeFieldOne() {
		DocTypeFieldDTO dto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_ONE_NAME)
			.fieldName(DOC_TYPE_FIELD_ONE_NAME)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.KEYWORD)
			.build();

		sessionFactory.withTransaction(
			s -> docTypeFieldService.create(dto)
		)
		.await()
		.indefinitely();
	}

	private void createDocTypeFieldTwo() {
		DocTypeFieldDTO dto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_TWO_NAME)
			.fieldName(DOC_TYPE_FIELD_TWO_NAME)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.KEYWORD)
			.build();

		sessionFactory.withTransaction(
				s -> docTypeFieldService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createDocTypeFieldThree() {
		DocTypeFieldDTO dto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_THREE_NAME)
			.fieldName(DOC_TYPE_FIELD_THREE_NAME)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.I18N)
			.build();

		sessionFactory.withTransaction(
				s -> docTypeFieldService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createDocTypeFieldFour() {
		DocTypeFieldDTO dto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_FOUR_NAME)
			.fieldName(DOC_TYPE_FIELD_FOUR_NAME)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.TEXT)
			.build();

		sessionFactory.withTransaction(
				s -> docTypeFieldService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createSuggestionCategoryOneWithDocTypeFieldOne() {
		SuggestionCategoryWithDocTypeFieldDTO dto = SuggestionCategoryWithDocTypeFieldDTO.builder()
			.name(SUGGESTION_CATEGORY_ONE_NAME)
			.priority(100f)
			.multiSelect(false)
			.docTypeFieldId(getDocTypeFieldOne().getId())
			.build();

		sessionFactory.withTransaction(
				s -> suggestionCategoryService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private List<DocTypeField> getAllFilteredDocTypeFields() {
		return sessionFactory.withTransaction(
				s -> docTypeFieldService.findAll()
			)
			.await()
			.indefinitely().stream()
				.filter(docTypeField -> docTypeField.isI18N() || docTypeField.isKeyword())
				.toList();
	}

	private List<DocTypeField> getAllTextDocTypeFields() {
		return sessionFactory.withTransaction(
				s -> docTypeFieldService.findAll()
			)
			.await()
			.indefinitely().stream()
			.filter(docTypeField -> docTypeField.isText())
			.toList();
	}

	private DocTypeField getDocTypeFieldOne() {
		return sessionFactory.withTransaction(
				s -> docTypeFieldService.findByName(s, DOC_TYPE_FIELD_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private DocTypeField getDocTypeFieldTwo() {
		return sessionFactory.withTransaction(
				s -> docTypeFieldService.findByName(s, DOC_TYPE_FIELD_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private DocTypeField getDocTypeFieldThree() {
		return sessionFactory.withTransaction(
				s -> docTypeFieldService.findByName(s, DOC_TYPE_FIELD_THREE_NAME)
			)
			.await()
			.indefinitely();
	}

	private DocTypeField getDocTypeFieldFour() {
		return sessionFactory.withTransaction(
				s -> docTypeFieldService.findByName(s, DOC_TYPE_FIELD_FOUR_NAME)
			)
			.await()
			.indefinitely();
	}

	private SuggestionCategory getSuggestionCategoryOne() {
		return sessionFactory.withTransaction(
				s -> suggestionCategoryService.findByName(s, SUGGESTION_CATEGORY_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private List<DocTypeField> getUnboundDocTypeFieldForMissingSuggestionCategory() {
		return sessionFactory.withTransaction(
			s ->
				suggestionCategoryService.findUnboundDocTypeFieldsBySuggestionCategory(0L)
		)
		.await()
		.indefinitely();
	}

	private List<DocTypeField> getUnboundDocTypeFieldForSuggestionCategoryOne() {
		var id = getSuggestionCategoryOne().getId();

		return sessionFactory.withTransaction(
				s ->
					suggestionCategoryService.findUnboundDocTypeFieldsBySuggestionCategory(id)
			)
			.await()
			.indefinitely();
	}

	private void removeDocTypeField(long id) {
		sessionFactory.withTransaction(
			session -> docTypeFieldService.deleteById(id)
		)
		.await()
		.indefinitely();
	}

	private void removeSuggestionCategory(long id) {
		sessionFactory.withTransaction(
				session -> suggestionCategoryService.deleteById(id)
			)
			.await()
			.indefinitely();
	}

	private void unsetDocTypeFieldFromSuggestionCategoryOne() {
		var id = getSuggestionCategoryOne().getId();

		sessionFactory.withTransaction(
			s -> suggestionCategoryService.unsetDocTypeField(id)
		)
			.await()
			.indefinitely();
	}
}
