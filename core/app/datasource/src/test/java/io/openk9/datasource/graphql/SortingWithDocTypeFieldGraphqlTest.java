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

package io.openk9.datasource.graphql;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.SortingService;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Verifies the {@code sortingWithDocTypeField}
 * mutation (create/patch/update router)
 * and the {@code deleteSorting}.
 *
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SortingWithDocTypeFieldGraphqlTest {

	private static final String ENTITY_NAME_PREFIX =
		"SortingWithDocTypeFieldGraphqlTest - ";

	private static final String DEFAULT_SORT = "defaultSort";
	private static final String DELETE_SORTING = "deleteSorting";
	private static final String DOC_TYPE_FIELD = "docTypeField";
	private static final String DOC_TYPE_FIELD_ID = "docTypeFieldId";
	private static final String ENTITY = "entity";
	private static final String FIELD = "field";
	private static final String FIELD_VALIDATORS = "fieldValidators";
	private static final String ID = "id";
	private static final float INIT_PRIORITY = 1.0f;
	private static final String MESSAGE = "message";
	private static final String NAME = "name";
	private static final long NON_EXISTENT_DOC_TYPE_FIELD_ID = 999_999_999L;
	private static final String PATCH = "patch";
	private static final float PATCHED_PRIORITY = 5.0f;
	private static final String PRIORITY = "priority";
	private static final String SORTING_A_NAME = ENTITY_NAME_PREFIX + "Sorting A";
	private static final String SORTING_DTO = "sortingWithDocTypeFieldDTO";
	private static final String SORTING_ID = "sortingId";
	private static final String SORTING_MUTATION = "sortingWithDocTypeField";
	private static final String SORTING_ORPHAN_NAME = ENTITY_NAME_PREFIX + "Sorting Orphan";
	private static final String TYPE = "type";

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	SortingService sortingService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Test
	@Order(1)
	void should_create_sorting_with_doc_type_field() throws Exception {
		var docTypeFieldId = getSampleDocTypeFieldId();
		assertTrue(docTypeFieldId > 0);

		var response = graphQLClient.executeSync(
			sortingMutation(
				null,
				false,
				inputObject(
					prop(NAME, SORTING_A_NAME),
					prop(PRIORITY, INIT_PRIORITY),
					prop(DEFAULT_SORT, false),
					prop(TYPE, Sorting.SortingType.ASC),
					prop(DOC_TYPE_FIELD_ID, docTypeFieldId)
				)
			)
		);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var sorting = getSorting(SORTING_A_NAME);

		assertNotNull(sorting);
		assertNotNull(sorting.getDocTypeField());
		assertEquals(docTypeFieldId, sorting.getDocTypeField().getId());
	}

	@Test
	@Order(2)
	void should_patch_sorting_keeping_doc_type_field_when_id_absent() throws Exception {
		var sorting = getSorting(SORTING_A_NAME);
		var boundDocTypeFieldId = sorting.getDocTypeField().getId();

		// patch without docTypeFieldId -> the existing link must be preserved
		var response = graphQLClient.executeSync(
			sortingMutation(
				sorting.getId(),
				true,
				inputObject(
					prop(NAME, SORTING_A_NAME),
					prop(PRIORITY, PATCHED_PRIORITY),
					prop(DEFAULT_SORT, false),
					prop(TYPE, Sorting.SortingType.ASC)
				)
			)
		);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var patched = getSorting(SORTING_A_NAME);

		assertEquals(PATCHED_PRIORITY, patched.getPriority());
		assertNotNull(patched.getDocTypeField());
		assertEquals(boundDocTypeFieldId, patched.getDocTypeField().getId());
	}

	@Test
	@Order(3)
	void should_update_sorting_clearing_doc_type_field_when_id_absent() throws Exception {
		var sorting = getSorting(SORTING_A_NAME);
		assertNotNull(sorting.getDocTypeField());

		// update without docTypeFieldId: set to null
		var response = graphQLClient.executeSync(
			sortingMutation(
				sorting.getId(),
				false,
				inputObject(
					prop(NAME, SORTING_A_NAME),
					prop(PRIORITY, INIT_PRIORITY),
					prop(DEFAULT_SORT, false),
					prop(TYPE, Sorting.SortingType.ASC)
				)
			)
		);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var updated = getSorting(SORTING_A_NAME);

		assertEquals(INIT_PRIORITY, updated.getPriority());
		assertNull(updated.getDocTypeField());
	}

	@Test
	@Order(4)
	void should_update_sorting_setting_doc_type_field_when_id_present() throws Exception {
		var docTypeFieldId = getSampleDocTypeFieldId();
		var sorting = getSorting(SORTING_A_NAME);
		assertNull(sorting.getDocTypeField());

		var response = graphQLClient.executeSync(
			sortingMutation(
				sorting.getId(),
				false,
				inputObject(
					prop(NAME, SORTING_A_NAME),
					prop(PRIORITY, INIT_PRIORITY),
					prop(DEFAULT_SORT, false),
					prop(TYPE, Sorting.SortingType.ASC),
					prop(DOC_TYPE_FIELD_ID, docTypeFieldId)
				)
			)
		);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var updated = getSorting(SORTING_A_NAME);

		assertNotNull(updated.getDocTypeField());
		assertEquals(docTypeFieldId, updated.getDocTypeField().getId());
	}

	@Test
	@Order(5)
	void should_return_field_validators_when_name_missing() throws Exception {
		// empty name violates @NotEmpty
		var response = graphQLClient.executeSync(
			sortingMutation(
				null,
				false,
				inputObject(
					prop(NAME, ""),
					prop(PRIORITY, INIT_PRIORITY),
					prop(DEFAULT_SORT, false),
					prop(TYPE, Sorting.SortingType.ASC)
				)
			)
		);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var payload = response.getData().getJsonObject(SORTING_MUTATION);

		// no entity, the violation is reported instead
		assertTrue(payload.isNull(ENTITY));
		assertFalse(payload.getJsonArray(FIELD_VALIDATORS).isEmpty());
	}

	@Test
	@Order(6)
	void should_delete_unbound_sorting() throws Exception {
		var sorting = getSorting(SORTING_A_NAME);
		assertNotNull(sorting);

		var response = graphQLClient.executeSync(
			document(
				operation(
					OperationType.MUTATION,
					field(
						DELETE_SORTING,
						args(arg(SORTING_ID, sorting.getId())),
						field(ID),
						field(NAME)
					)
				)
			)
		);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		assertNull(getSortingOrNull(SORTING_A_NAME));
	}

	@Test
	@Order(7)
	void should_not_persist_sorting_with_non_existent_doc_type_field() throws Exception {
		// docTypeFieldId points to a missing row: fk_sorting_doc_type_field must
		// reject the insert so no orphan sorting is persisted
		try {
			graphQLClient.executeSync(
				sortingMutation(
					null,
					false,
					inputObject(
						prop(NAME, SORTING_ORPHAN_NAME),
						prop(PRIORITY, INIT_PRIORITY),
						prop(DEFAULT_SORT, false),
						prop(TYPE, Sorting.SortingType.ASC),
						prop(DOC_TYPE_FIELD_ID, NON_EXISTENT_DOC_TYPE_FIELD_ID)
					)
				)
			);
		}
		catch (Exception ignored) {
			// the constraint violation may surface as a transport error
		}

		assertNull(getSortingOrNull(SORTING_ORPHAN_NAME));
	}

	@Test
	@Order(8)
	void tearDown() {
		removeSortingIfExists(SORTING_A_NAME);
		removeSortingIfExists(SORTING_ORPHAN_NAME);
	}

	private io.smallrye.graphql.client.core.Document sortingMutation(
		Long id, boolean patch, io.smallrye.graphql.client.core.InputObject dto) {

		var arguments = id == null
			? args(
				arg(PATCH, patch),
				arg(SORTING_DTO, dto))
			: args(
				arg(ID, id),
				arg(PATCH, patch),
				arg(SORTING_DTO, dto));

		return document(
			operation(
				OperationType.MUTATION,
				field(
					SORTING_MUTATION,
					arguments,
					field(
						ENTITY,
						field(ID),
						field(NAME),
						field(PRIORITY),
						field(
							DOC_TYPE_FIELD,
							field(ID),
							field(NAME)
						)
					),
					field(
						FIELD_VALIDATORS,
						field(FIELD),
						field(MESSAGE)
					)
				)
			)
		);
	}

	private Long getSampleDocTypeFieldId() {
		return EntitiesUtils.getSampleTextDocTypeFieldId(
			docTypeFieldService, sessionFactory);
	}

	private Sorting getSorting(String name) {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					sortingService.findByName(s, name)
						.call(sorting -> Mutiny.fetch(sorting.getDocTypeField()))
			)
			.await()
			.indefinitely();
	}

	private Sorting getSortingOrNull(String name) {
		try {
			return getSorting(name);
		}
		catch (NoResultException e) {
			return null;
		}
	}

	private void removeSortingIfExists(String name) {
		var sorting = getSortingOrNull(name);

		if (sorting != null) {
			sessionFactory.withTransaction(
					(s, transaction) -> sortingService.deleteById(s, sorting.getId())
				)
				.await()
				.indefinitely();
		}
	}
}
