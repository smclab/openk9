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
import io.openk9.datasource.model.Autocorrection;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.SortType;
import io.openk9.datasource.model.SuggestMode;
import io.openk9.datasource.model.dto.base.AutocorrectionDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class AutocorrectionCRUDTest {

	private static final Logger log = Logger.getLogger(AutocorrectionCRUDTest.class);
	private static final int MAX_EDIT = 2;
	private static final int MAX_EDIT_UPDATED = 1;
	private static final int MIN_WORD_LENGTH = 3;
	private static final int MIN_WORD_LENGTH_UPDATED = 4;
	private static final int PREFIX_LENGTH = 3;
	private static final int PREFIX_LENGTH_UPDATED = 4;
	private static final String ENTITY_NAME_PREFIX = "AutocorrectionCRUDTest - ";
	private static final String AUTOCORRECTION_NAME_ONE = ENTITY_NAME_PREFIX + "Autocorrection 1";
	private static final String AUTOCORRECTION_NAME_TWO = ENTITY_NAME_PREFIX + "Autocorrection 2";

	@Inject
	AutocorrectionService autocorrectionService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@BeforeEach
	void setup() {
		var docTypeFields = getAllDocTypeFields();

		var docTypeFieldId = docTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.map(K9Entity::getId)
			.findFirst()
			.orElse(0L);

		AutocorrectionDTO autocorrectionDTOTwo = AutocorrectionDTO.builder()
			.name(AUTOCORRECTION_NAME_TWO)
			.autocorrectionDocTypeFieldId(docTypeFieldId)
			.maxEdit(MAX_EDIT)
			.minWordLength(MIN_WORD_LENGTH)
			.prefixLength(PREFIX_LENGTH)
			.build();

		EntitiesUtils.createAutocorrection(
			sessionFactory,
			autocorrectionService,
			autocorrectionDTOTwo
		);

	}

	@Test
	void should_create_empty_autocorrection_one() {
		AutocorrectionDTO dto = AutocorrectionDTO.builder()
			.name(AUTOCORRECTION_NAME_ONE)
			.build();

		EntitiesUtils.createAutocorrection(sessionFactory, autocorrectionService, dto);

		Autocorrection autocorrection =
			EntitiesUtils.getAutocorrection(
				AUTOCORRECTION_NAME_ONE,
				autocorrectionService,
				sessionFactory
			);

		log.debug(String.format("Autocorrection: %s", autocorrection.toString()));

		assertNull(autocorrection.getAutocorrectionDocTypeField());
		assertEquals(AUTOCORRECTION_NAME_ONE, autocorrection.getName());
		assertEquals(SortType.SCORE, autocorrection.getSort());
		assertEquals(SuggestMode.MISSING, autocorrection.getSuggestMode());
		assertNull(autocorrection.getPrefixLength());
		assertNull(autocorrection.getMinWordLength());
		assertNull(autocorrection.getMaxEdit());

		EntitiesUtils.removeEntity(
			AUTOCORRECTION_NAME_ONE,
			autocorrectionService,
			sessionFactory
		);
	}

	@Test
	void should_create_autocorrection_one_with_docTypeField() {
		var docTypeFields = getAllDocTypeFields();

		docTypeFields.forEach(docTypeField ->
			log.debug(
				String.format(
					"Field name: %s, type: %s.",
					docTypeField.getName(),
					docTypeField.getFieldType().name()
				)
			)
		);

		var sampleTextDocTypeField = docTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.findFirst();

		assertTrue(sampleTextDocTypeField.isPresent());
		var sampleTextDocTypeFieldId = sampleTextDocTypeField.get().getId();

		var dto = AutocorrectionDTO.builder()
			.name(AUTOCORRECTION_NAME_ONE)
			.autocorrectionDocTypeFieldId(sampleTextDocTypeFieldId)
			.prefixLength(PREFIX_LENGTH)
			.minWordLength(MIN_WORD_LENGTH)
			.maxEdit(MAX_EDIT)
			.build();

		log.debug(
			String.format(
				"DocTypeField from sample, of type TEXT, with id: %d",
				sampleTextDocTypeFieldId
			)
		);

		EntitiesUtils.createAutocorrection(sessionFactory, autocorrectionService, dto);

		Autocorrection autocorrection =
			EntitiesUtils.getAutocorrection(
				AUTOCORRECTION_NAME_ONE,
				autocorrectionService,
				sessionFactory
			);

		log.debug(String.format("Autocorrection: %s", autocorrection.toString()));

		assertEquals(AUTOCORRECTION_NAME_ONE, autocorrection.getName());
		assertEquals(SortType.SCORE, autocorrection.getSort());
		assertEquals(SuggestMode.MISSING, autocorrection.getSuggestMode());
		assertEquals(PREFIX_LENGTH, autocorrection.getPrefixLength());
		assertEquals(MIN_WORD_LENGTH, autocorrection.getMinWordLength());
		assertEquals(MAX_EDIT, autocorrection.getMaxEdit());
		assertNotNull(autocorrection.getAutocorrectionDocTypeField());
		assertEquals(
			sampleTextDocTypeFieldId,
			autocorrection.getAutocorrectionDocTypeField().getId()
		);

		EntitiesUtils.removeEntity(
			AUTOCORRECTION_NAME_ONE,
			autocorrectionService,
			sessionFactory
		);
	}

	@Test
	void should_patch_autocorrection_two() {
		Autocorrection autocorrectionTwo = EntitiesUtils.getAutocorrection(
			AUTOCORRECTION_NAME_TWO,
			autocorrectionService,
			sessionFactory
		);

		// Initial check
		assertEquals(MAX_EDIT, autocorrectionTwo.getMaxEdit());
		assertEquals(MIN_WORD_LENGTH, autocorrectionTwo.getMinWordLength());
		assertEquals(PREFIX_LENGTH, autocorrectionTwo.getPrefixLength());
		assertNotNull(autocorrectionTwo.getAutocorrectionDocTypeField());

		log.debug(
			String.format(
				"Initial DocTypeField with id: %d",
				autocorrectionTwo.getAutocorrectionDocTypeField().getId()
			)
		);

		var docTypeFields = getAllDocTypeFields();

		var docTypeField = docTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.filter(field -> !Objects.equals(field.getId(), autocorrectionTwo.getAutocorrectionDocTypeField().getId()))
			.findFirst();

		assertTrue(docTypeField.isPresent());
		var docTypeFieldId = docTypeField.get().getId();

		assertNotEquals(autocorrectionTwo.getAutocorrectionDocTypeField().getId(), docTypeFieldId);

		var dto = AutocorrectionDTO.builder()
			.name(AUTOCORRECTION_NAME_TWO)
			.autocorrectionDocTypeFieldId(docTypeFieldId)
			.sort(SortType.FREQUENCY)
			.suggestMode(SuggestMode.ALWAYS)
			.maxEdit(MAX_EDIT_UPDATED)
			.minWordLength(MIN_WORD_LENGTH_UPDATED)
			.prefixLength(PREFIX_LENGTH_UPDATED)
			.build();

		log.debug(
			String.format(
				"Patch with DocTypeField from sample, of type TEXT, with id: %d",
				docTypeFieldId
			)
		);

		patchAutocorrection(autocorrectionTwo.getId(), dto);

		Autocorrection newAutocorrection =
			EntitiesUtils.getAutocorrection(
				AUTOCORRECTION_NAME_TWO,
				autocorrectionService,
				sessionFactory
			);

		log.debug(String.format("Autocorrection: %s", newAutocorrection.toString()));

		assertEquals(AUTOCORRECTION_NAME_TWO, newAutocorrection.getName());
		assertEquals(PREFIX_LENGTH_UPDATED, newAutocorrection.getPrefixLength());
		assertEquals(MIN_WORD_LENGTH_UPDATED, newAutocorrection.getMinWordLength());
		assertEquals(MAX_EDIT_UPDATED, newAutocorrection.getMaxEdit());
		assertEquals(SortType.FREQUENCY, newAutocorrection.getSort());
		assertEquals(SuggestMode.ALWAYS, newAutocorrection.getSuggestMode());
		assertNotNull(newAutocorrection.getAutocorrectionDocTypeField());
		assertEquals(
			docTypeFieldId,
			newAutocorrection.getAutocorrectionDocTypeField().getId()
		);
	}

	@Test
	void should_update_autocorrection_two() {
		Autocorrection autocorrectionTwo = EntitiesUtils.getAutocorrection(
			AUTOCORRECTION_NAME_TWO,
			autocorrectionService,
			sessionFactory
		);

		// Initial check
		assertEquals(MAX_EDIT, autocorrectionTwo.getMaxEdit());
		assertEquals(MIN_WORD_LENGTH, autocorrectionTwo.getMinWordLength());
		assertEquals(PREFIX_LENGTH, autocorrectionTwo.getPrefixLength());
		assertNotNull(autocorrectionTwo.getAutocorrectionDocTypeField());

		log.debug(
			String.format(
				"Initial DocTypeField with id: %d",
				autocorrectionTwo.getAutocorrectionDocTypeField().getId()
			)
		);

		var docTypeFields = getAllDocTypeFields();

		var docTypeField = docTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.filter(field -> !Objects.equals(field.getId(), autocorrectionTwo.getAutocorrectionDocTypeField().getId()))
			.findFirst();

		assertTrue(docTypeField.isPresent());
		var docTypeFieldId = docTypeField.get().getId();

		assertNotEquals(autocorrectionTwo.getAutocorrectionDocTypeField().getId(), docTypeFieldId);

		var dto = AutocorrectionDTO.builder()
			.name(AUTOCORRECTION_NAME_TWO)
			.autocorrectionDocTypeFieldId(docTypeFieldId)
			.sort(SortType.FREQUENCY)
			.suggestMode(SuggestMode.ALWAYS)
			.maxEdit(MAX_EDIT_UPDATED)
			.minWordLength(MIN_WORD_LENGTH_UPDATED)
			.prefixLength(PREFIX_LENGTH_UPDATED)
			.build();

		log.debug(
			String.format(
				"Update with DocTypeField from sample, of type TEXT, with id: %d",
				docTypeFieldId
			)
		);

		updateAutocorrection(autocorrectionTwo.getId(), dto);

		Autocorrection newAutocorrection =
			EntitiesUtils.getAutocorrection(
				AUTOCORRECTION_NAME_TWO,
				autocorrectionService,
				sessionFactory
			);

		log.debug(String.format("Autocorrection: %s", newAutocorrection.toString()));

		assertEquals(AUTOCORRECTION_NAME_TWO, newAutocorrection.getName());
		assertEquals(PREFIX_LENGTH_UPDATED, newAutocorrection.getPrefixLength());
		assertEquals(MIN_WORD_LENGTH_UPDATED, newAutocorrection.getMinWordLength());
		assertEquals(MAX_EDIT_UPDATED, newAutocorrection.getMaxEdit());
		assertEquals(SortType.FREQUENCY, newAutocorrection.getSort());
		assertEquals(SuggestMode.ALWAYS, newAutocorrection.getSuggestMode());
		assertNotNull(newAutocorrection.getAutocorrectionDocTypeField());
		assertEquals(
			docTypeFieldId,
			newAutocorrection.getAutocorrectionDocTypeField().getId()
		);
	}

	@AfterEach
	void tearDown() {
		EntitiesUtils.removeEntity(
			AUTOCORRECTION_NAME_TWO,
			autocorrectionService,
			sessionFactory
		);
	}

	private void patchAutocorrection(long id, AutocorrectionDTO dto) {
		sessionFactory.withTransaction(session ->
				autocorrectionService.patch(id, dto)
			)
			.await()
			.indefinitely();
	}

	private void updateAutocorrection(long id, AutocorrectionDTO dto) {
		sessionFactory.withTransaction(session ->
			autocorrectionService.update(id, dto)
		)
		.await()
		.indefinitely();
	}

	private List<DocTypeField> getAllDocTypeFields() {
		return sessionFactory.withTransaction(session ->
				docTypeFieldService.findAll()
			)
			.await().indefinitely();
	}
}
