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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.dto.base.TabDTO;
import io.openk9.datasource.model.dto.base.TokenTabDTO;
import io.openk9.datasource.service.TabService;
import io.openk9.datasource.service.TokenTabService;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TabWithTokenTabGraphqlTest {

	private static final String ENTITY_NAME_PREFIX = "TabWithTokenTabGraphqlTest - ";

	private static final String EDGES = "edges";
	private static final String ENTITY = "entity";
	private static final String ID = "id";
	private static final int INIT_PRIORITY = 0;
	private static final String NAME = "name";
	private static final String NODE = "node";
	private static final String PATCH = "patch";
	private static final int PATCHED_PRIORITY = 10;
	private static final String PRIORITY = "priority";
	private static final String TAB_ONE_NAME = ENTITY_NAME_PREFIX + "Tab 1";
	private static final String TAB_WITH_TOKEN_TABS = "tabWithTokenTabs";
	private static final String TAB_WITH_TOKEN_TABS_DTO = "tabWithTokenTabsDTO";
	private static final String TOKEN_TABS = "tokenTabs";
	private static final String TOKEN_TAB_IDS = "tokenTabIds";
	private static final String TOKEN_TAB_ONE_NAME = ENTITY_NAME_PREFIX + "TokenTab 1";
	private static final String TOKEN_TAB_TWO_NAME = ENTITY_NAME_PREFIX + "TokenTab 2";

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	TabService tabService;

	@Inject
	TokenTabService tokenTabService;

	@Test
	@Order(1)
	void init() {
		createTabOne();
		createTokenTabOne();
		createTokenTabTwo();
	}

	@Test
	@Order(2)
	void should_patch_tab_without_token_tab() throws ExecutionException, InterruptedException {
		var tabOne = getTabOne();

		assertEquals(INIT_PRIORITY, tabOne.getPriority());
		assertTrue(tabOne.getTokenTabs().isEmpty());

		var query = document(
			operation(
				OperationType.MUTATION,
				field(
					TAB_WITH_TOKEN_TABS,
					args(
						arg(ID, tabOne.getId()),
						arg(PATCH, true),
						arg(
							TAB_WITH_TOKEN_TABS_DTO,
							inputObject(
								prop(NAME, TAB_ONE_NAME),
								prop(PRIORITY, PATCHED_PRIORITY)
							)
						)
					),
					field(
						ENTITY,
						field(ID),
						field(NAME),
						field(
							TOKEN_TABS,
							field(
								EDGES,
								field(
									NODE,
									field(ID),
									field(NAME)
								)
							)
						)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var tabOnePatched = getTabOne();

		assertEquals(PATCHED_PRIORITY, tabOnePatched.getPriority());
		assertTrue(tabOnePatched.getTokenTabs().isEmpty());
	}

	@Test
	@Order(3)
	void should_update_tab_without_token_tab() throws ExecutionException, InterruptedException {
		var tabOne = getTabOne();

		assertEquals(PATCHED_PRIORITY, tabOne.getPriority());
		assertTrue(tabOne.getTokenTabs().isEmpty());

		var query = document(
			operation(
				OperationType.MUTATION,
				field(
					TAB_WITH_TOKEN_TABS,
					args(
						arg(ID, tabOne.getId()),
						arg(PATCH, false),
						arg(
							TAB_WITH_TOKEN_TABS_DTO,
							inputObject(
								prop(NAME, TAB_ONE_NAME),
								prop(PRIORITY, INIT_PRIORITY)
							)
						)
					),
					field(
						ENTITY,
						field(ID),
						field(NAME),
						field(
							TOKEN_TABS,
							field(
								EDGES,
								field(
									NODE,
									field(ID),
									field(NAME)
								)
							)
						)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var tabOneUpdated = getTabOne();

		assertEquals(INIT_PRIORITY, tabOneUpdated.getPriority());
		assertTrue(tabOneUpdated.getTokenTabs().isEmpty());
	}

	@Test
	@Order(4)
	void should_update_tab_with_two_token_tabs() throws ExecutionException, InterruptedException {
		var tabOne = getTabOne();

		assertEquals(INIT_PRIORITY, tabOne.getPriority());
		assertTrue(tabOne.getTokenTabs().isEmpty());

		var tokenTabOne = getTokenTabOne();
		var tokenTabTwo = getTokenTabTwo();
		var tokenTabs = List.of(tokenTabOne.getId(), tokenTabTwo.getId());

		var query = document(
			operation(
				OperationType.MUTATION,
				field(
					TAB_WITH_TOKEN_TABS,
					args(
						arg(ID, tabOne.getId()),
						arg(PATCH, false),
						arg(
							TAB_WITH_TOKEN_TABS_DTO,
							inputObject(
								prop(NAME, TAB_ONE_NAME),
								prop(PRIORITY, PATCHED_PRIORITY),
								prop(TOKEN_TAB_IDS, tokenTabs)
							)
						)
					),
					field(
						ENTITY,
						field(ID),
						field(NAME),
						field(
							TOKEN_TABS,
							field(
								EDGES,
								field(
									NODE,
									field(ID),
									field(NAME)
								)
							)
						)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var tabOneUpdated = getTabOne();

		assertEquals(PATCHED_PRIORITY, tabOneUpdated.getPriority());
		assertEquals(
			tokenTabs,
			tabOneUpdated.getTokenTabs().stream()
				.map(TokenTab::getId)
				.toList()
		);
	}

	@Test
	@Order(5)
	void should_update_tab_removing_two_token_tabs() throws ExecutionException, InterruptedException {
		var tabOne = getTabOne();
		var tokenTabOne = getTokenTabOne();
		var tokenTabTwo = getTokenTabTwo();
		var tokenTabs = List.of(tokenTabOne.getId(), tokenTabTwo.getId());

		assertEquals(PATCHED_PRIORITY, tabOne.getPriority());
		assertEquals(
			tokenTabs,
			tabOne.getTokenTabs().stream()
				.map(TokenTab::getId)
				.toList()
		);


		var query = document(
			operation(
				OperationType.MUTATION,
				field(
					TAB_WITH_TOKEN_TABS,
					args(
						arg(ID, tabOne.getId()),
						arg(PATCH, false),
						arg(
							TAB_WITH_TOKEN_TABS_DTO,
							inputObject(
								prop(NAME, TAB_ONE_NAME),
								prop(PRIORITY, INIT_PRIORITY),
								prop(TOKEN_TAB_IDS, new ArrayList<>())
							)
						)
					),
					field(
						ENTITY,
						field(ID),
						field(NAME),
						field(
							TOKEN_TABS,
							field(
								EDGES,
								field(
									NODE,
									field(ID),
									field(NAME)
								)
							)
						)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var tabOneUpdated = getTabOne();

		assertEquals(INIT_PRIORITY, tabOneUpdated.getPriority());
		assertTrue(tabOneUpdated.getTokenTabs().isEmpty());
	}

	@Test
	@Order(6)
	void tearDown() {
		removeTabOne();
		removeTokenTabOne();
		removeTokenTabTwo();

		assertThrows(NoResultException.class, this::getTabOne);
		assertThrows(NoResultException.class, this::getTokenTabOne);
		assertThrows(NoResultException.class, this::getTokenTabTwo);
	}

	private void createTabOne() {
		TabDTO dto = TabDTO.builder()
			.name(TAB_ONE_NAME)
			.priority(INIT_PRIORITY)
			.build();

		sessionFactory.withTransaction(
				(s,transaction) ->
					tabService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createTokenTabOne() {
		TokenTabDTO dto = TokenTabDTO.builder()
			.name(TOKEN_TAB_ONE_NAME)
			.tokenType(TokenTab.TokenType.TEXT)
			.value("test")
			.filter(false)
			.build();

		sessionFactory.withTransaction(
				(s,transaction) ->
					tokenTabService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private void createTokenTabTwo() {
		TokenTabDTO dto = TokenTabDTO.builder()
			.name(TOKEN_TAB_TWO_NAME)
			.tokenType(TokenTab.TokenType.TEXT)
			.value("test")
			.filter(false)
			.build();

		sessionFactory.withTransaction(
				(s,transaction) ->
					tokenTabService.create(dto)
			)
			.await()
			.indefinitely();
	}

	private Tab getTabOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.findByName(s, TAB_ONE_NAME)
						.call(tab -> Mutiny.fetch(tab.getTokenTabs()))
			)
			.await()
			.indefinitely();
	}

	private TokenTab getTokenTabOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tokenTabService.findByName(s, TOKEN_TAB_ONE_NAME)
			)
			.await()
			.indefinitely();
	}

	private TokenTab getTokenTabTwo() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					tokenTabService.findByName(s, TOKEN_TAB_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private void removeTabOne() {
		var tabId = getTabOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tabService.deleteById(tabId)
			)
			.await()
			.indefinitely();
	}

	private void removeTokenTabOne() {
		var tokenTabId = getTokenTabOne().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tokenTabService.deleteById(tokenTabId)
			)
			.await()
			.indefinitely();
	}

	private void removeTokenTabTwo() {
		var tokenTabId = getTokenTabTwo().getId();

		sessionFactory.withTransaction(
				(s, transaction) ->
					tokenTabService.deleteById(tokenTabId)
			)
			.await()
			.indefinitely();
	}
}
