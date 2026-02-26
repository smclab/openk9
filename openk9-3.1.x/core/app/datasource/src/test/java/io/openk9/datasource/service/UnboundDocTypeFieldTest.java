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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.BaseDocTypeField;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.base.AutocompleteDTO;
import io.openk9.datasource.model.dto.base.AutocorrectionDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.request.SuggestionCategoryWithDocTypeFieldDTO;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnboundDocTypeFieldTest {

	private static final String ENTITY_NAME_PREFIX = "UnboundDocTypeFieldTest - ";
	private static final String DOC_TYPE_FIELD_ONE_NAME = ENTITY_NAME_PREFIX + "Doc type field 1";
	private static final String DOC_TYPE_FIELD_TWO_NAME = ENTITY_NAME_PREFIX + "Doc type field 2";
	private static final String DOC_TYPE_FIELD_THREE_NAME = ENTITY_NAME_PREFIX + "Doc type field 3";
	private static final String DOC_TYPE_FIELD_FOUR_NAME = ENTITY_NAME_PREFIX + "Doc type field 4";
	private static final String DOC_TYPE_FIELD_FIVE_NAME = ENTITY_NAME_PREFIX + "Doc type field 5";
	private static final String DOC_TYPE_FIELD_SIX_NAME = ENTITY_NAME_PREFIX + "Doc type field 6";
	private static final String DOC_TYPE_FIELD_SEVEN_NAME = ENTITY_NAME_PREFIX + "Doc type field 7";
	private static final String DOC_TYPE_FIELD_EIGHT_NAME = ENTITY_NAME_PREFIX + "Doc type field 8";
	private static final String SUGGESTION_CATEGORY_ONE_NAME = ENTITY_NAME_PREFIX + "Suggestion category 1";
	private static final String AUTOCORRECTION_ONE_NAME = ENTITY_NAME_PREFIX + "Autocorrection 1";
	private static final String AUTOCOMPLETE_ONE_NAME = ENTITY_NAME_PREFIX + "Autocomplete 1";

	@Inject
	AutocompleteService autocompleteService;

	@Inject
	AutocorrectionService autocorrectionService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	Mutiny.SessionFactory sf;

	@BeforeEach
	void setup() {
		//1. docTypeField with "KEYWORD" to bound to the suggestionCategoryOne
		createDocTypeFieldKeyword(DOC_TYPE_FIELD_ONE_NAME);
		//2. docTypeField with "KEYWORD" to leave unbound
		createDocTypeFieldKeyword(DOC_TYPE_FIELD_TWO_NAME);
		//3. docTypeField with "I18N" to leave unbound
		createDocTypeFieldI18N(DOC_TYPE_FIELD_THREE_NAME);
		//4. docTypeField without either "KEYWORD" or "I18N" to leave unbound
		var docTypeFieldFour = createDocTypeFieldText(DOC_TYPE_FIELD_FOUR_NAME);
		//5-6. two docTypeFields with "SEARCH_AS_YOU_TYPE" to bound to the AutocompleteOne
		createDocTypeFieldSearchAsYouType(docTypeFieldFour.getId(), DOC_TYPE_FIELD_FIVE_NAME);
		createDocTypeFieldSearchAsYouType(docTypeFieldFour.getId(), DOC_TYPE_FIELD_SIX_NAME);
		//7. docTypeField with "SEARCH_AS_YOU_TYPE" to leave unbound
		createDocTypeFieldSearchAsYouType(docTypeFieldFour.getId(), DOC_TYPE_FIELD_SEVEN_NAME);
		//8. docTypeField of type "SEARCH_AS_YOU_TYPE" but without parent to leave unbound
		createDocTypeFieldSearchAsYouType(null, DOC_TYPE_FIELD_EIGHT_NAME);

		//create suggestion category and bound with docTypeFieldOne
		createSuggestionCategoryOneWithDocTypeFieldOne();

		//create Autocorrection and bound with docTypeFieldOne
		var docTypeFieldOne =
			EntitiesUtils.getEntity(DOC_TYPE_FIELD_ONE_NAME, docTypeFieldService, sf);

		AutocorrectionDTO autocorrectionDTO = AutocorrectionDTO.builder()
			.name(AUTOCORRECTION_ONE_NAME)
			.autocorrectionDocTypeFieldId(docTypeFieldOne.getId())
			.build();

		EntitiesUtils.createEntity(autocorrectionDTO, autocorrectionService, sf);

		// Create Autocomplete and bound with docTypeFieldFive and docTypeFieldSix.
		// This two fields are child of the docTypeFieldFour.
		var docTypeFieldFive =
			EntitiesUtils.getEntity(DOC_TYPE_FIELD_FIVE_NAME, docTypeFieldService, sf);
		var docTypeFieldSix =
			EntitiesUtils.getEntity(DOC_TYPE_FIELD_SIX_NAME, docTypeFieldService, sf);

		AutocompleteDTO autocompleteDTO = AutocompleteDTO.builder()
			.name(AUTOCOMPLETE_ONE_NAME)
			.fieldIds(Set.of(docTypeFieldFive.getId(), docTypeFieldSix.getId()))
			.build();

		EntitiesUtils.createEntity(autocompleteDTO, autocompleteService, sf);
	}

	// Unbound by SuggestionCategory
	@Test
	void should_retrieve_all_filtered_unbound_doc_type_fields() {
		var actualUnboundDocTypeFields = getUnboundDocTypeFieldForSuggestionCategoryOne();
		var boundDocTypeFieldId = getSuggestionCategoryOne().getDocTypeField().getId();

		// Expected unbound DocTypeField are all filtered DocTypeField
		// except the one bound to the given SuggestionCategory.
		var expectedUnboundDocTypeFields = getAllFilteredDocTypeFields().stream()
			.filter(docTypeField ->
				!Objects.equals(docTypeField.getId(), boundDocTypeFieldId))
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
			EntitiesUtils.getEntity(AUTOCORRECTION_ONE_NAME, autocorrectionService, sf);

		var actualUnboundDocTypeFields =
			sf.withTransaction(s ->
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
			.filter(docTypeField ->
				!Objects.equals(docTypeField.getId(), boundDocTypeFieldId))
			.toList();

		assertFalse(actualUnboundDocTypeFields.isEmpty());
		assertEquals(expectedUnboundDocTypeFields.size(), actualUnboundDocTypeFields.size());
		assertEquals(expectedUnboundDocTypeFields, actualUnboundDocTypeFields);

	}

	@Test
	void should_retrieve_all_textual_doc_type_fields_from_missing_autocorrection() {
		var actualUnboundDocTypeFields = sf.withTransaction(s ->
			docTypeFieldService.findUnboundDocTypeFieldByAutocorrection(0L)
		)
		.await()
		.indefinitely();

		// Expected unbound DocTypeField are all filtered DocTypeField
		var expectedUnboundDocTypeFields = getAllTextDocTypeFields();

		assertFalse(actualUnboundDocTypeFields.isEmpty());
		assertEquals(expectedUnboundDocTypeFields, actualUnboundDocTypeFields);
	}

	// Unbound by Autocomplete
	@Test
	void should_retrieve_all_searchAsYouType_unbound_doc_type_fields() {
		var autocomplete =
			EntitiesUtils.getAutocomplete(AUTOCOMPLETE_ONE_NAME, autocompleteService, sf);

		var actualUnboundDocTypeFields =
			sf.withTransaction(s ->
					docTypeFieldService.findUnboundDocTypeFieldByAutocomplete(
						autocomplete.getId()
					)
				)
				.await()
				.indefinitely();

		var boundDocTypeFields = autocomplete.getFields();

		assertNotNull(boundDocTypeFields);
		var boundDocTypeFieldId = boundDocTypeFields.stream()
			.map(DocTypeField::getId)
			.collect(Collectors.toSet());

		// Expected unbound DocTypeField are all search_as_you_type with parent DocTypeField
		// except the two bound to the given Autocomplete.
		var expectedUnboundDocTypeFields = getAllAutocompleteBoundableDocTypeFields().stream()
			.filter(docTypeField ->
				!boundDocTypeFieldId.contains(docTypeField.getId()))
			.toList();

		assertFalse(actualUnboundDocTypeFields.isEmpty());
		assertEquals(expectedUnboundDocTypeFields.size(), actualUnboundDocTypeFields.size());
		assertEquals(expectedUnboundDocTypeFields, actualUnboundDocTypeFields);

	}

	@Test
	@DisplayName("Should be retrieved all docTypeField boundable to an Autocomplete")
	void should_retrieve_all_boundable_doc_type_fields_from_missing_autocomplete() {
		var actualUnboundDocTypeFields = sf.withTransaction(s ->
				docTypeFieldService.findUnboundDocTypeFieldByAutocomplete(0L)
			)
			.await()
			.indefinitely();

		// Expected unbound DocTypeField are all filtered DocTypeField
		var expectedUnboundDocTypeFields = getAllAutocompleteBoundableDocTypeFields();

		assertFalse(actualUnboundDocTypeFields.isEmpty());
		assertEquals(expectedUnboundDocTypeFields, actualUnboundDocTypeFields);
	}

	@AfterEach
	void tearDown() {
		//remove suggestion
		removeSuggestionCategory(getSuggestionCategoryOne().getId());

		//remove doc type fields
		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_ONE_NAME, docTypeFieldService, sf);
		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_TWO_NAME, docTypeFieldService, sf);
		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_THREE_NAME, docTypeFieldService, sf);

		//removes autocorrection and autocomplete
		EntitiesUtils.removeEntity(AUTOCORRECTION_ONE_NAME, autocorrectionService, sf);
		EntitiesUtils.removeEntity(AUTOCOMPLETE_ONE_NAME, autocompleteService, sf);

		//remove remaining doc type fields
		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_FIVE_NAME, docTypeFieldService, sf);
		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_SIX_NAME, docTypeFieldService, sf);
		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_SEVEN_NAME, docTypeFieldService, sf);
		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_FOUR_NAME, docTypeFieldService, sf);
	}

	private void createDocTypeFieldKeyword(String name) {
		DocTypeFieldDTO dto = DocTypeFieldDTO.builder()
			.name(name)
			.fieldName(name)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.KEYWORD)
			.build();

		sf.withTransaction(
			s -> docTypeFieldService.create(dto)
		)
		.await()
		.indefinitely();
	}

	private void createDocTypeFieldI18N(String name) {
		DocTypeFieldDTO dto = DocTypeFieldDTO.builder()
			.name(name)
			.fieldName(name)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.I18N)
			.build();

		sf.withTransaction(
				s -> docTypeFieldService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private DocTypeField createDocTypeFieldText(String name) {
		DocTypeFieldDTO dto = DocTypeFieldDTO.builder()
			.name(name)
			.fieldName(name)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.TEXT)
			.build();

		return sf.withTransaction(
				s -> docTypeFieldService.create(dto)
			)
			.await()
			.indefinitely();
	}

	/**
	 * Creates a search-as-you-type field for a document type.
	 * <p>
	 * The field is configured as non-searchable and non-sortable. If a parent identifier is provided,
	 * the field is created as a sub-field of the parent; otherwise, it is created as a top-level field.
	 * </p>
	 *
	 * @param parentId the identifier of the parent field, or {@code null} for a top-level field
	 * @param name the name of the field
	 */
	private void createDocTypeFieldSearchAsYouType(Long parentId, String name) {
		DocTypeFieldDTO dto = DocTypeFieldDTO.builder()
			.name(name)
			.fieldName(name)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.SEARCH_AS_YOU_TYPE)
			.build();

		if (parentId != null) {
			EntitiesUtils.createSubField(parentId, dto, docTypeFieldService);
		}
		else {
			EntitiesUtils.createEntity(dto, docTypeFieldService, sf);
		}
	}

	private void createSuggestionCategoryOneWithDocTypeFieldOne() {

		var docTypeFieldOne =
			EntitiesUtils.getEntity(DOC_TYPE_FIELD_ONE_NAME, docTypeFieldService, sf);

		SuggestionCategoryWithDocTypeFieldDTO dto = SuggestionCategoryWithDocTypeFieldDTO.builder()
			.name(SUGGESTION_CATEGORY_ONE_NAME)
			.priority(100f)
			.multiSelect(false)
			.docTypeFieldId(docTypeFieldOne.getId())
			.build();

		sf.withTransaction(
				s -> suggestionCategoryService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private List<DocTypeField> getAllFilteredDocTypeFields() {
		return sf.withTransaction(
				s -> docTypeFieldService.findAll()
			)
			.await()
			.indefinitely().stream()
				.filter(docTypeField ->
					docTypeField.isI18N() || docTypeField.isKeyword())
				.toList();
	}

	/**
	 * Retrieve all docTypeField boundable with an Autocomplete entity.
	 * </p>
	 * A docTypeField is considered boundable if it is of type SEARCH_AS_YOU_TYPE, and it has a parent.
	 *
	 * @return a List of boundable docTypeField.
	 */
	private List<DocTypeField> getAllAutocompleteBoundableDocTypeFields() {
		var allFields = sf.withTransaction(
				s -> docTypeFieldService.findAll()
			)
			.await()
			.indefinitely();

			var autocompleteList = allFields.stream()
			.filter(BaseDocTypeField::isAutocomplete)
			.toList();

			var boundableList = autocompleteList
				.stream()
				.filter(field -> field.getParentDocTypeField() != null)
			.toList();

			return boundableList;
	}

	private List<DocTypeField> getAllTextDocTypeFields() {
		return sf.withTransaction(
				s -> docTypeFieldService.findAll()
			)
			.await()
			.indefinitely().stream()
			.filter(BaseDocTypeField::isText)
			.toList();
	}

	private SuggestionCategory getSuggestionCategoryOne() {
		return sf.withTransaction(
				s -> suggestionCategoryService.findByName(s, SUGGESTION_CATEGORY_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private List<DocTypeField> getUnboundDocTypeFieldForMissingSuggestionCategory() {
		return sf.withTransaction(
			s ->
				suggestionCategoryService.findUnboundDocTypeFieldsBySuggestionCategory(0L)
		)
		.await()
		.indefinitely();
	}

	private List<DocTypeField> getUnboundDocTypeFieldForSuggestionCategoryOne() {
		var id = getSuggestionCategoryOne().getId();

		return sf.withTransaction(
				s ->
					suggestionCategoryService.findUnboundDocTypeFieldsBySuggestionCategory(id)
			)
			.await()
			.indefinitely();
	}

	private void removeSuggestionCategory(long id) {
		sf.withTransaction(
				session -> suggestionCategoryService.deleteById(id)
			)
			.await()
			.indefinitely();
	}

	private void unsetDocTypeFieldFromSuggestionCategoryOne() {
		var id = getSuggestionCategoryOne().getId();

		sf.withTransaction(
			s -> suggestionCategoryService.unsetDocTypeField(id)
		)
			.await()
			.indefinitely();
	}
}
