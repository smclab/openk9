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
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
public class AutocorrectionCRUDTest {

	public static final int MAX_EDITS = 2;
	public static final int MIN_WORD_LENGTH = 3;
	public static final int PREFIX_LENGTH = 3;
	private static final Logger log = Logger.getLogger(AutocorrectionCRUDTest.class);
	private static final String ENTITY_NAME_PREFIX = "AutocorrectionCRUDTest - ";
	private static final String AUTOCORRECTION_NAME_ONE = ENTITY_NAME_PREFIX + "Autocorrection 1";

	@Inject
	AutocorrectionService autocorrectionService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Test
	void should_create_empty_autocorrection_one() {
		var docTypeFields = getAllDocTypeFields();

		docTypeFields.forEach(docTypeField ->
			System.out.println(
				String.format(
					"Field name: %s, type: %s.",
					docTypeField.getName(),
					docTypeField.getFieldType().name()
				)
			)
		);

		Long sampleTextDocTypeFieldId = docTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.map(DocTypeField::getId)
			.findFirst()
			.orElse(0L);

		log.debug(String.format("DocTypeField from sample, of type TEXT, with id: %d", sampleTextDocTypeFieldId));

		AutocorrectionDTO dto = AutocorrectionDTO.builder()
			.name(AUTOCORRECTION_NAME_ONE)
//			.autocorrectionDocTypeFieldId(sampleTextDocTypeFieldId)
			.build();

		createAutocorrection(dto);

		Autocorrection autocorrection =
			EntitiesUtils.getAutocorrection(
				sessionFactory,
				autocorrectionService,
				AUTOCORRECTION_NAME_ONE
			);

		log.debug(String.format("Autocorrection: %s", autocorrection.toString()));
		assertNull(autocorrection.getAutocorrectionDocTypeField());
		assertEquals(AUTOCORRECTION_NAME_ONE, autocorrection.getName());
		assertEquals(SortType.SCORE, autocorrection.getSort());
		assertEquals(SuggestMode.MISSING, autocorrection.getSuggestMode());
		assertNull(autocorrection.getPrefixLength());
		assertNull(autocorrection.getMinWordLength());
		assertNull(autocorrection.getMaxEdit());

		EntitiesUtils.removeAutocorrection(
			sessionFactory,
			autocorrectionService,
			AUTOCORRECTION_NAME_ONE
		);
	}

	@Test
	void should_create_autocorrection_one_with_docTypeField() {
		var docTypeFields = getAllDocTypeFields();

		docTypeFields.forEach(docTypeField ->
			System.out.println(
				String.format(
					"Field name: %s, type: %s.",
					docTypeField.getName(),
					docTypeField.getFieldType().name()
				)
			)
		);

		Long sampleTextDocTypeFieldId = docTypeFields.stream()
			.filter(field -> "sample".equalsIgnoreCase(field.getDocType().getName()))
			.filter(field -> FieldType.TEXT.equals(field.getFieldType()))
			.map(DocTypeField::getId)
			.findFirst()
			.orElse(0L);

		log.debug(String.format("DocTypeField from sample, of type TEXT, with id: %d", sampleTextDocTypeFieldId));

		AutocorrectionDTO dto = AutocorrectionDTO.builder()
			.name(AUTOCORRECTION_NAME_ONE)
			.autocorrectionDocTypeFieldId(sampleTextDocTypeFieldId)
			.prefixLength(PREFIX_LENGTH)
			.minWordLength(MIN_WORD_LENGTH)
			.maxEdit(MAX_EDITS)
			.build();

		createAutocorrection(dto);

		Autocorrection autocorrection =
			EntitiesUtils.getAutocorrection(
				sessionFactory,
				autocorrectionService,
				AUTOCORRECTION_NAME_ONE
			);

		log.debug(String.format("Autocorrection: %s", autocorrection.toString()));

		assertEquals(AUTOCORRECTION_NAME_ONE, autocorrection.getName());
		assertEquals(SortType.SCORE, autocorrection.getSort());
		assertEquals(SuggestMode.MISSING, autocorrection.getSuggestMode());
		assertEquals(PREFIX_LENGTH, autocorrection.getPrefixLength());
		assertEquals(MIN_WORD_LENGTH, autocorrection.getMinWordLength());
		assertEquals(MAX_EDITS, autocorrection.getMaxEdit());
		assertNotNull(autocorrection.getAutocorrectionDocTypeField());
		assertEquals(
			sampleTextDocTypeFieldId,
			autocorrection.getAutocorrectionDocTypeField().getId()
		);

		EntitiesUtils.removeAutocorrection(
			sessionFactory,
			autocorrectionService,
			AUTOCORRECTION_NAME_ONE
		);
	}

	private Autocorrection createAutocorrection(AutocorrectionDTO dto) {
		return sessionFactory.withTransaction(
			session -> autocorrectionService.create(dto)
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
