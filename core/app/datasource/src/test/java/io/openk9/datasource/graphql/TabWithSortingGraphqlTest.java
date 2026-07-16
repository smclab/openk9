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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.dto.base.SortingDTO;
import io.openk9.datasource.model.dto.base.TabDTO;
import io.openk9.datasource.service.SortingService;
import io.openk9.datasource.service.TabService;

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
 * Verifies the Tab with Sorting association added to the
 * {@code tabWithTokenTabs} mutation.
 *
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TabWithSortingGraphqlTest {

	private static final String ENTITY_NAME_PREFIX = "TabWithSortingGraphqlTest - ";

	private static final String DELETE_SORTING = "deleteSorting";
	private static final String ENTITY = "entity";
	private static final String ID = "id";
	private static final int INIT_PRIORITY = 0;
	private static final String NAME = "name";
	private static final String PATCH = "patch";
	private static final int PATCHED_PRIORITY = 10;
	private static final String PRIORITY = "priority";
	private static final String SORTING_ID = "sortingId";
	private static final String SORTING_IDS = "sortingIds";
	private static final String SORTING_ONE_NAME = ENTITY_NAME_PREFIX + "Sorting 1";
	private static final String SORTING_TWO_NAME = ENTITY_NAME_PREFIX + "Sorting 2";
	private static final String TAB_ONE_NAME = ENTITY_NAME_PREFIX + "Tab 1";
	private static final String TAB_TWO_NAME = ENTITY_NAME_PREFIX + "Tab 2";
	private static final String TAB_WITH_TOKEN_TABS = "tabWithTokenTabs";
	private static final String TAB_WITH_TOKEN_TABS_DTO = "tabWithTokenTabsDTO";

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	SortingService sortingService;

	@Inject
	TabService tabService;

	@Test
	@Order(1)
	void init() {
		createTab(TAB_ONE_NAME);
		createSorting(SORTING_ONE_NAME);
		createSorting(SORTING_TWO_NAME);
	}

	@Test
	@Order(2)
	void should_create_tab_with_sortings() throws Exception {
		var sortingIds = List.of(
			getSorting(SORTING_ONE_NAME).getId(),
			getSorting(SORTING_TWO_NAME).getId()
		);

		var response = graphQLClient.executeSync(
			tabMutation(
				null,
				false,
				inputObject(
					prop(NAME, TAB_TWO_NAME),
					prop(PRIORITY, INIT_PRIORITY),
					prop(SORTING_IDS, sortingIds)
				)
			)
		);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		assertEquals(Set.copyOf(sortingIds), sortingIdsOf(TAB_TWO_NAME));
	}

	@Test
	@Order(3)
	void should_update_tab_associating_two_sortings() throws Exception {
		var sortingIds = List.of(
			getSorting(SORTING_ONE_NAME).getId(),
			getSorting(SORTING_TWO_NAME).getId()
		);

		var response = graphQLClient.executeSync(
			tabMutation(
				getTab(TAB_ONE_NAME).getId(),
				false,
				inputObject(
					prop(NAME, TAB_ONE_NAME),
					prop(PRIORITY, PATCHED_PRIORITY),
					prop(SORTING_IDS, sortingIds)
				)
			)
		);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		assertEquals(Set.copyOf(sortingIds), sortingIdsOf(TAB_ONE_NAME));
	}

	@Test
	@Order(4)
	void should_patch_tab_preserving_sortings_when_ids_absent() throws Exception {
		var boundBefore = sortingIdsOf(TAB_ONE_NAME);
		assertEquals(2, boundBefore.size());

		// patch without sortingIds -> the collection must be left untouched
		var response = graphQLClient.executeSync(
			tabMutation(
				getTab(TAB_ONE_NAME).getId(),
				true,
				inputObject(
					prop(NAME, TAB_ONE_NAME),
					prop(PRIORITY, INIT_PRIORITY)
				)
			)
		);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		assertEquals(boundBefore, sortingIdsOf(TAB_ONE_NAME));
	}

	@Test
	@Order(5)
	void should_patch_tab_replacing_sortings_when_ids_present() throws Exception {
		var sortingOneId = getSorting(SORTING_ONE_NAME).getId();

		// patch with sortingIds -> the collection is realigned to the provided ids
		var response = graphQLClient.executeSync(
			tabMutation(
				getTab(TAB_ONE_NAME).getId(),
				true,
				inputObject(
					prop(NAME, TAB_ONE_NAME),
					prop(PRIORITY, INIT_PRIORITY),
					prop(SORTING_IDS, List.of(sortingOneId))
				)
			)
		);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		assertEquals(Set.of(sortingOneId), sortingIdsOf(TAB_ONE_NAME));
	}

	@Test
	@Order(6)
	void should_update_tab_clearing_sortings_when_ids_absent() throws Exception {
		assertFalse(sortingIdsOf(TAB_ONE_NAME).isEmpty());

		// update without sortingIds, all sortings removed
		var response = graphQLClient.executeSync(
			tabMutation(
				getTab(TAB_ONE_NAME).getId(),
				false,
				inputObject(
					prop(NAME, TAB_ONE_NAME),
					prop(PRIORITY, INIT_PRIORITY)
				)
			)
		);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		assertTrue(sortingIdsOf(TAB_ONE_NAME).isEmpty());
	}

	@Test
	@Order(7)
	void should_fail_deleting_sorting_bound_to_tab() throws Exception {
		var sortingOneId = getSorting(SORTING_ONE_NAME).getId();

		// re-bind the sorting to the tab
		graphQLClient.executeSync(
			tabMutation(
				getTab(TAB_ONE_NAME).getId(),
				false,
				inputObject(
					prop(NAME, TAB_ONE_NAME),
					prop(PRIORITY, INIT_PRIORITY),
					prop(SORTING_IDS, List.of(sortingOneId))
				)
			)
		);
		assertEquals(Set.of(sortingOneId), sortingIdsOf(TAB_ONE_NAME));

		// deleting a sorting still referenced by tab_sorting must fail
		boolean failed;
		try {
			var response = graphQLClient.executeSync(
				document(
					operation(
						OperationType.MUTATION,
						field(
							DELETE_SORTING,
							args(arg(SORTING_ID, sortingOneId)),
							field(ID)
						)
					)
				)
			);
			failed = response.hasError();
		}
		catch (Exception e) {
			failed = true;
		}

		assertTrue(
			failed,
			"deleting a sorting still bound to a tab is expected to fail");

		// the sorting must still be there, unaffected by the failed delete
		assertEquals(sortingOneId, getSorting(SORTING_ONE_NAME).getId());
	}

	@Test
	@Order(8)
	void tearDown() {
		removeTabIfExists(TAB_ONE_NAME);
		removeTabIfExists(TAB_TWO_NAME);
		removeSortingIfExists(SORTING_ONE_NAME);
		removeSortingIfExists(SORTING_TWO_NAME);

		assertThrows(NoResultException.class, () -> getTab(TAB_ONE_NAME));
		assertThrows(NoResultException.class, () -> getTab(TAB_TWO_NAME));
		assertThrows(NoResultException.class, () -> getSorting(SORTING_ONE_NAME));
		assertThrows(NoResultException.class, () -> getSorting(SORTING_TWO_NAME));
	}

	private io.smallrye.graphql.client.core.Document tabMutation(
		Long id, boolean patch, io.smallrye.graphql.client.core.InputObject dto) {

		var arguments = id == null
			? args(
				arg(PATCH, patch),
				arg(TAB_WITH_TOKEN_TABS_DTO, dto))
			: args(
				arg(ID, id),
				arg(PATCH, patch),
				arg(TAB_WITH_TOKEN_TABS_DTO, dto));

		return document(
			operation(
				OperationType.MUTATION,
				field(
					TAB_WITH_TOKEN_TABS,
					arguments,
					field(
						ENTITY,
						field(ID),
						field(NAME)
					)
				)
			)
		);
	}

	private void createTab(String name) {
		TabDTO dto = TabDTO.builder()
			.name(name)
			.priority(INIT_PRIORITY)
			.build();

		EntitiesUtils.createEntity(dto, tabService, sessionFactory);
	}

	private void createSorting(String name) {
		SortingDTO dto = SortingDTO.builder()
			.name(name)
			.priority(1.0f)
			.defaultSort(false)
			.type(Sorting.SortingType.ASC)
			.build();

		EntitiesUtils.createEntity(dto, sortingService, sessionFactory);
	}

	private Tab getTab(String name) {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.findByName(s, name)
						.call(tab -> Mutiny.fetch(tab.getSortings()))
			)
			.await()
			.indefinitely();
	}

	private Sorting getSorting(String name) {
		return sessionFactory.withTransaction(
				(s, transaction) -> sortingService.findByName(s, name)
			)
			.await()
			.indefinitely();
	}

	private Set<Long> sortingIdsOf(String tabName) {
		return getTab(tabName).getSortings().stream()
			.map(Sorting::getId)
			.collect(Collectors.toSet());
	}

	private void removeTabIfExists(String name) {
		try {
			var tab = getTab(name);
			sessionFactory.withTransaction(
					(s, transaction) -> tabService.deleteById(s, tab.getId())
				)
				.await()
				.indefinitely();
		}
		catch (NoResultException e) {
			// already absent
		}
	}

	private void removeSortingIfExists(String name) {
		try {
			var sorting = getSorting(name);
			sessionFactory.withTransaction(
					(s, transaction) -> sortingService.deleteById(s, sorting.getId())
				)
				.await()
				.indefinitely();
		}
		catch (NoResultException e) {
			// already absent
		}
	}
}
