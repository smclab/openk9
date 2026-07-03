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
import io.openk9.datasource.model.util.K9Entity;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
public class HighlightServiceTest {

	@Inject
	HighlightService highlightService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	private static final String FIELD_1 = "field1";
	private static final String FIELD_2 = "field2";
	private static final String HIGHLIGHT_NAME = "highlightTest";
	private static final List<String> DOC_TYPE_FIELD_LIST =
		List.of(FIELD_1, FIELD_2);

	@BeforeEach
	void setup() {
		DOC_TYPE_FIELD_LIST.forEach(this::createDocTypeField);
	}

	@Test
	void should_create_highlight_with_only_mandatory_fields() {
		highlightService.create(
			HighlightDTO.builder()
				.name(HIGHLIGHT_NAME)
				.type(Highlight.HighlightType.UNIFIED)
				.fieldIds(Set.of(getDocTypeFieldId(FIELD_1)))
				.build()
		).await().indefinitely();

		var result = EntitiesUtils
			.getEntity(HIGHLIGHT_NAME, highlightService, sessionFactory);

		assertNotNull(result);
		assertEquals(Highlight.HighlightType.UNIFIED, result.getType());
	}

	@Test
	void should_fail_when_fields_are_missing() {
		var highlight = HighlightDTO.builder()
			.name(HIGHLIGHT_NAME)
			.type(Highlight.HighlightType.UNIFIED)
			.order(Highlight.OrderType.NONE)
			.build();

		var response = highlightService.getValidator()
			.create(highlight)
			.await()
			.indefinitely();

		assertNull(response.getEntity());
		assertFalse(response.getFieldValidators().isEmpty());
	}

	@Test
	void should_find_all_highlights() {
		highlightService.create(
			HighlightDTO.builder()
				.name(HIGHLIGHT_NAME + "1")
				.type(Highlight.HighlightType.FVH)
				.fieldIds(Set.of(getDocTypeFieldId(FIELD_1)))
				.boundaryScanner(Highlight.BoundaryScannerType.SENTENCE)
				.numberOfFragments(5)
				.build()
		).await().indefinitely();

		highlightService.create(
			HighlightDTO.builder()
				.name(HIGHLIGHT_NAME + "2")
				.type(Highlight.HighlightType.PLAIN)
				.fieldIds(Set.of(getDocTypeFieldId(FIELD_2)))
				.fragmenter(Highlight.FragmenterType.SPAN)
				.numberOfFragments(8)
				.build()
		).await().indefinitely();

		var result = highlightService.findAll().await().indefinitely();

		assertNotNull(result);
		assertEquals(Set.of(HIGHLIGHT_NAME + "1", HIGHLIGHT_NAME + "2"),
			result.stream()
				.map(Highlight::getName)
				.collect(Collectors.toSet())
		);
	}

	@Test
	void should_update_fields() {
		var highlight = highlightService.create(
			HighlightDTO.builder()
				.name(HIGHLIGHT_NAME)
				.type(Highlight.HighlightType.UNIFIED)
				.fieldIds(Set.of(getDocTypeFieldId(FIELD_1)))
				.build()
		).await().indefinitely();

		highlightService.update(
			highlight.getId(),
			HighlightDTO.builder()
				.name("highlight")
				.type(Highlight.HighlightType.FVH)
				.fieldIds(Set.of(getDocTypeFieldId(FIELD_1), getDocTypeFieldId(FIELD_2)))
				.build()
		).await().indefinitely();

		var result = sessionFactory.withTransaction((s, t) ->
			highlightService.findByName(s, "highlight")
				.call(h -> Mutiny.fetch(h.getFields()))
		).await().indefinitely();

		assertEquals(
			Set.of(getDocTypeFieldId(FIELD_1), getDocTypeFieldId(FIELD_2)),
			result.getFields()
				.stream()
				.map(K9Entity::getId)
				.collect(Collectors.toSet())
		);
	}

	@Test
	void should_delete_highlight() {
		var highlight = highlightService.create(
			HighlightDTO.builder()
				.name(HIGHLIGHT_NAME)
				.type(Highlight.HighlightType.UNIFIED)
				.fieldIds(Set.of(getDocTypeFieldId(FIELD_1)))
				.build()
		).await().indefinitely();

		highlightService.deleteById(highlight.getId())
			.await()
			.indefinitely();

		var result = highlightService.findById(highlight.getId())
			.await()
			.indefinitely();

		assertNull(result);
	}

	@AfterEach
	void tearDown() {
		highlightService.findAll()
			.await()
			.indefinitely()
			.forEach(highlight ->
				highlightService.deleteById(highlight.getId())
					.await()
					.indefinitely()
			);

		DOC_TYPE_FIELD_LIST.forEach(name ->
			EntitiesUtils.removeEntity(name, docTypeFieldService, sessionFactory));
	}

	private Long getDocTypeFieldId(String name) {
		return EntitiesUtils.getEntity(name, docTypeFieldService, sessionFactory).getId();
	}

	private void createDocTypeField(String name) {
		var docTypeField = DocTypeFieldDTO.builder()
			.name(name)
			.fieldName(name)
			.searchable(true)
			.sortable(true)
			.fieldType(FieldType.BINARY)
			.build();

		EntitiesUtils.createEntity(
			docTypeField,
			docTypeFieldService,
			sessionFactory
		);
	}
}
