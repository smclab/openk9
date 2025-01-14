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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import jakarta.inject.Inject;

import io.openk9.datasource.graphql.dto.SuggestionCategoryWithDocTypeFieldDTO;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SuggestionCategoryTest {

	private static final String PUBLIC_TENANT = "public";
	private static final String SGCWITHFIELD_1 = "sgcwithfield1";
	private static final String EMTPY_JSON_CONFIG = "{}";
	private static final String SGCTESTFIELD_1 = "sgctestfield1";
	private static final String SGCTESTFIELD_2 = "sgctestfield2";
	private static final String SGCTESTFIELD_3 = "sgctestfield3";
	private static final Logger log = Logger.getLogger(SuggestionCategoryTest.class);

	private final List<Long> docTypeFieldIds = new LinkedList<>();
	private SuggestionCategory suggestionCategory;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Test
	@Order(1)
	void setup() {

		log.info("Trying to setup SuggestionCategoryTest");

		var field1 = docTypeFieldService.create(DocTypeFieldDTO.builder()
				.name(SGCTESTFIELD_1)
				.fieldName(SGCTESTFIELD_1)
				.jsonConfig(EMTPY_JSON_CONFIG)
				.fieldType(FieldType.TEXT)
				.build())
			.await().indefinitely().getId();

		var field2 = docTypeFieldService.create(DocTypeFieldDTO.builder()
				.name(SGCTESTFIELD_2)
				.fieldName(SGCTESTFIELD_2)
				.jsonConfig(EMTPY_JSON_CONFIG)
				.fieldType(FieldType.TEXT)
				.build())
			.await().indefinitely().getId();

		var field3 = docTypeFieldService.create(DocTypeFieldDTO.builder()
				.name(SGCTESTFIELD_3)
				.fieldName(SGCTESTFIELD_3)
				.jsonConfig(EMTPY_JSON_CONFIG)
				.fieldType(FieldType.TEXT)
				.build())
			.await().indefinitely().getId();

		docTypeFieldIds.add(field1);
		docTypeFieldIds.add(field2);
		docTypeFieldIds.add(field3);

		log.info("SuggestionCategoryTest ready");

	}

	@Test
	@Order(2)
	void should_create_suggestionCategory_with_first_docTypeField() {

		var created = suggestionCategoryService.create(
				SuggestionCategoryWithDocTypeFieldDTO.builder()
					.name(SGCWITHFIELD_1)
					.priority(100f)
					.multiSelect(false)
					.docTypeFieldId(docTypeFieldIds.getFirst())
					.build()
			)
			.await().indefinitely();

		this.suggestionCategory = getSuggestionCategory(created.getId());
		var docTypeField = suggestionCategory.getDocTypeField();

		Assertions.assertEquals(SGCWITHFIELD_1, suggestionCategory.getName());
		Assertions.assertNotNull(docTypeField);
		Assertions.assertEquals(docTypeFieldIds.getFirst(), docTypeField.getId());

	}

	@Test
	@Order(3)
	void should_update_suggestionCategory_with_second_docTypeField() {

		var updated = suggestionCategoryService.update(
			suggestionCategory.getId(),
			SuggestionCategoryWithDocTypeFieldDTO.builder()
				.name(suggestionCategory.getName())
				.priority(200f)
				.docTypeFieldId(docTypeFieldIds.get(1))
				.build()
		).await().indefinitely();

		this.suggestionCategory = getSuggestionCategory(updated.getId());
		var docTypeField = suggestionCategory.getDocTypeField();

		Assertions.assertEquals(SGCWITHFIELD_1, suggestionCategory.getName());
		Assertions.assertEquals(200f, suggestionCategory.getPriority());
		Assertions.assertNotNull(docTypeField);
		Assertions.assertEquals(docTypeFieldIds.get(1), docTypeField.getId());

	}

	@Test
	@Order(4)
	void should_patch_suggestionCategory_with_last_docTypeField() {

		var updated = suggestionCategoryService.patch(
			suggestionCategory.getId(),
			SuggestionCategoryWithDocTypeFieldDTO.builder()
				.name(suggestionCategory.getName())
				.docTypeFieldId(docTypeFieldIds.getLast())
				.build()
		).await().indefinitely();

		this.suggestionCategory = getSuggestionCategory(updated.getId());
		var docTypeField = suggestionCategory.getDocTypeField();

		Assertions.assertEquals(SGCWITHFIELD_1, suggestionCategory.getName());
		Assertions.assertEquals(200f, suggestionCategory.getPriority());
		Assertions.assertNotNull(docTypeField);
		Assertions.assertEquals(docTypeFieldIds.getLast(), docTypeField.getId());

	}

	@Test
	@Order(6)
	void should_delete_suggestionCategory() {
		suggestionCategoryService.deleteById(suggestionCategory.getId())
			.await().indefinitely();

		var fetched = suggestionCategoryService.findById(suggestionCategory.getId())
			.await().indefinitely();

		Assertions.assertNull(fetched);
	}

	@Test
	@Order(5)
	void should_find_by_searchText_suggestionCategory_connection() {

		var emptyByName = suggestionCategoryService.findConnection(
				null, null, null, null, "a string that is not contained", null
				)
				.await()
				.indefinitely();

		var emptyByPriority = suggestionCategoryService.findConnection(
				null, null, null, null, "140", null
			)
			.await()
			.indefinitely();

		var byName = suggestionCategoryService.findConnection(
				null, null, null, null, SGCWITHFIELD_1, null)
			.await().indefinitely();


		var byPriority = suggestionCategoryService.findConnection(
				null, null, null, null, "200", null)
			.await().indefinitely();

		Assertions.assertTrue(emptyByName.getEdges().isEmpty());
		Assertions.assertTrue(emptyByPriority.getEdges().isEmpty());

		Assertions.assertEquals(1, byName.getEdges().size());
		Assertions.assertEquals(1, byPriority.getEdges().size());
	}

	@Test
	@Order(7)
	void tearDown() {

		log.info("Trying to teardown SuggestionCategoryTest");

		sessionFactory.withTransaction((s, t) -> {
			List<Uni<DocTypeField>> deleteUnis = new ArrayList<>();

			for (Long docTypeFieldId : docTypeFieldIds) {

				deleteUnis.add(
					docTypeFieldService.deleteById(s, docTypeFieldId));

			}

			return Uni.join().all(deleteUnis)
				.usingConcurrencyOf(1)
				.andFailFast();

		}).await().indefinitely();

		log.info("SuggestionCategoryTest passed");

	}

	private SuggestionCategory getSuggestionCategory(long id) {
		return sessionFactory.withTransaction(PUBLIC_TENANT, (s, t) ->
			suggestionCategoryService.findById(s, id)
				.call(sc -> Mutiny.fetch(sc.getDocTypeField()))
		).await().indefinitely();
	}

}
