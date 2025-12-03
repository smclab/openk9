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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.Autocomplete;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.dto.base.AutocompleteDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.service.exception.InvalidDocTypeFieldSetException;
import io.openk9.datasource.validation.ValidAutocompleteFields;

import io.quarkus.test.junit.QuarkusTest;
import org.hibernate.HibernateException;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AutocompleteCRUDTest {

	private static final Logger log = Logger.getLogger(AutocompleteCRUDTest.class);

	private static final String ENTITY_NAME_PREFIX = "AutocompleteCRUDTest - ";
	private static final String AUTOCOMPLETE_NAME_ONE = ENTITY_NAME_PREFIX + "Autocomplete 1";
	private static final String AUTOCOMPLETE_NAME_TWO = ENTITY_NAME_PREFIX + "Autocomplete 2";
	private static final String DOC_TYPE_FIELD_NAME_ONE = ENTITY_NAME_PREFIX + "Doc type field 1";
	private static final String DOC_TYPE_FIELD_NAME_TWO = ENTITY_NAME_PREFIX + "Doc type field 2";

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	AutocompleteService service;

	@Inject
	Mutiny.SessionFactory sf;

	@BeforeEach
	void setup() {

		DocTypeFieldDTO fieldDtoOne = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME_ONE)
			.fieldName("searchasyoutype")
			.fieldType(FieldType.SEARCH_AS_YOU_TYPE)
			.build();
		DocTypeFieldDTO fieldDtoTwo = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_NAME_TWO)
			.fieldName("searchasyoutype")
			.fieldType(FieldType.SEARCH_AS_YOU_TYPE)
			.build();

		EntitiesUtils.createEntity(fieldDtoOne, docTypeFieldService, sf);
		EntitiesUtils.createEntity(fieldDtoTwo, docTypeFieldService, sf);

		var fieldIds =
			EntitiesUtils.getAllSearchAsYouTypeDocTypeField(docTypeFieldService, sf).stream()
				.map(DocTypeField::getId)
				.collect(Collectors.toSet());

		AutocompleteDTO autocompleteDTOTwo = AutocompleteDTO.builder()
			.name(AUTOCOMPLETE_NAME_TWO)
			.fieldIds(fieldIds)
			.build();

		EntitiesUtils.createEntity(autocompleteDTOTwo, service, sf);
	}

	@Test
	void should_create_empty_autocomplete() {
		var fieldIds =
			EntitiesUtils.getAllSearchAsYouTypeDocTypeField(docTypeFieldService, sf).stream()
				.map(DocTypeField::getId)
				.collect(Collectors.toSet());

		AutocompleteDTO dto = AutocompleteDTO.builder()
			.name(AUTOCOMPLETE_NAME_ONE)
			.fieldIds(fieldIds)
			.build();

		EntitiesUtils.createEntity(dto, service, sf);

		var autocomplete = EntitiesUtils.getAutocomplete(AUTOCOMPLETE_NAME_ONE, service, sf);

		log.debugf("Autocomplete from DB: %s", autocomplete);

		assertEquals(
			Autocomplete.RESULT_SIZE,
			autocomplete.getResultSize());
		assertEquals(fieldIds.size(), autocomplete.getFields().size());
		assertTrue(
			autocomplete.getFields().stream()
				.allMatch(DocTypeField::isAutocomplete)
		);
		assertEquals(Autocomplete.DEFAULT_FUZZINESS, autocomplete.getFuzziness());
		assertEquals(
			Autocomplete.DEFAULT_MINIMUM_SHOULD_MATCH,
			autocomplete.getMinimumShouldMatch());
		assertEquals(Autocomplete.DEFAULT_OPERATOR, autocomplete.getOperator());

		EntitiesUtils.removeEntity(AUTOCOMPLETE_NAME_ONE, service, sf);
	}

	@Test
	void should_fail_creating_autocomplete_with_no_searchAsYouType_field() throws NoSuchMethodException {
		var fieldId = EntitiesUtils.getSampleTextDocTypeFieldId(docTypeFieldService, sf);

		AutocompleteDTO dto = AutocompleteDTO.builder()
			.name(AUTOCOMPLETE_NAME_ONE)
			.fieldIds(Set.of(fieldId))
			.build();

		var exception = assertThrows(
			HibernateException.class, () -> EntitiesUtils.createEntity(dto, service, sf));

		var defaultValidatorMessage = ValidAutocompleteFields.class
			.getMethod("message")
			.getDefaultValue()
			.toString();

		assertTrue(exception.getMessage().contains(defaultValidatorMessage));
		log.errorf(exception, "Exception message: %s\n\n", exception.getMessage());
	}

	@Test
	void should_fail_patching_autocomplete_with_no_searchAsYouType_field() throws NoSuchMethodException {
		var textFieldId = EntitiesUtils.getSampleTextDocTypeFieldId(docTypeFieldService, sf);

		var updateFieldIds =
			EntitiesUtils.getAllSearchAsYouTypeDocTypeField(docTypeFieldService, sf).stream()
				.map(DocTypeField::getId)
				.collect(Collectors.toSet());

		updateFieldIds.add(textFieldId);

		AutocompleteDTO dto = AutocompleteDTO.builder()
			.name(AUTOCOMPLETE_NAME_TWO)
			.fieldIds(updateFieldIds)
			.build();

		var autocompleteToUpdate = EntitiesUtils.getEntity(AUTOCOMPLETE_NAME_TWO, service, sf);

		var exception = assertThrows(
			InvalidDocTypeFieldSetException.class,
			() -> updateAutocomplete(autocompleteToUpdate.getId(), dto)
		);

		log.errorf(exception, "Exception message: %s\n\n", exception.getMessage());
	}

	@Test
	void should_fail_updating_autocomplete_with_no_searchAsYouType_field() throws NoSuchMethodException {
		var textFieldId = EntitiesUtils.getSampleTextDocTypeFieldId(docTypeFieldService, sf);

		var updateFieldIds =
			EntitiesUtils.getAllSearchAsYouTypeDocTypeField(docTypeFieldService, sf).stream()
				.map(DocTypeField::getId)
				.collect(Collectors.toSet());

		updateFieldIds.add(textFieldId);

		AutocompleteDTO dto = AutocompleteDTO.builder()
			.name(AUTOCOMPLETE_NAME_TWO)
			.fieldIds(updateFieldIds)
			.build();

		var autocompleteToUpdate = EntitiesUtils.getEntity(AUTOCOMPLETE_NAME_TWO, service, sf);

		var exception = assertThrows(
			InvalidDocTypeFieldSetException.class,
			() -> patchAutocomplete(autocompleteToUpdate.getId(), dto)
		);

		log.errorf(exception, "Exception message: %s\n\n", exception.getMessage());
	}

	@AfterEach
	void tearDown() {
		EntitiesUtils.removeEntity(AUTOCOMPLETE_NAME_TWO, service, sf);

		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_NAME_ONE, docTypeFieldService, sf);
		EntitiesUtils.removeEntity(DOC_TYPE_FIELD_NAME_TWO, docTypeFieldService, sf);
	}

	private void patchAutocomplete(Long id, AutocompleteDTO dto) {

		sf.withTransaction(
				session -> service.patch(id, dto)
			)
			.await()
			.indefinitely();
	}

	private void updateAutocomplete(Long id, AutocompleteDTO dto) {

		sf.withTransaction(
				session -> service.update(id, dto)
			)
			.await()
			.indefinitely();
	}
}
