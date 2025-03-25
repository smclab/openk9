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

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Enum.gqlEnum;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.inject.Inject;

import io.openk9.datasource.Initializer;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.dto.request.BucketWithListsDTO;
import io.openk9.datasource.model.util.K9Entity;

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
public class CreateBucketTest {

	private static final String CREATE_BUCKET_TEST_NAME = "CreateBucketTest";

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	BucketService bucketService;

	@Inject
	DatasourceService datasourceService;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	TabService tabService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Test
	@Order(1)
	void should_create_bucket_with_lists() {

		var datasourceIds = datasourceService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var suggestionCategorieIds = suggestionCategoryService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var tabIds = tabService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var bucket = bucketService.create(BucketWithListsDTO.builder()
			.name(CREATE_BUCKET_TEST_NAME)
			.refreshOnDate(true)
			.refreshOnQuery(true)
			.refreshOnTab(true)
			.refreshOnSuggestionCategory(true)
			.retrieveType(Bucket.RetrieveType.MATCH)
			.datasourceIds(datasourceIds)
			.suggestionCategoryIds(suggestionCategorieIds)
			.tabIds(tabIds)
			.build()
		).await().indefinitely();

		var bucketId = bucket.getId();

		var createdBucket = sessionFactory.withTransaction((s, t) ->
			bucketService.findById(s, bucketId)
				.call(entity -> Mutiny.fetch(entity.getDatasources()))
				.call(entity -> Mutiny.fetch(entity.getSuggestionCategories()))
				.call(entity -> Mutiny.fetch(entity.getTabs()))
		).await().indefinitely();

		assertEquals(CREATE_BUCKET_TEST_NAME, createdBucket.getName());
		assertEquals(datasourceIds.size(), createdBucket.getDatasources().size());

		var dataSources = createdBucket.getDatasources().iterator();
		assertEquals(Initializer.INIT_DATASOURCE_CONNECTION, dataSources.next().getName());

		assertEquals(suggestionCategorieIds.size(), createdBucket.getSuggestionCategories().size());
		assertEquals(tabIds.size(), createdBucket.getTabs().size());

	}

	@Test
	@Order(2)
	void should_patch_bucket_with_lists() {

		var bucket = sessionFactory.withTransaction((s, t) ->
			bucketService.findByName(s, CREATE_BUCKET_TEST_NAME)
				.call(entity -> Mutiny.fetch(entity.getTabs()))
		).await().indefinitely();

		bucketService.patch(bucket.getId(), BucketWithListsDTO.builder()
			.name(CREATE_BUCKET_TEST_NAME)
			.refreshOnDate(true)
			.refreshOnQuery(true)
			.refreshOnTab(true)
			.refreshOnSuggestionCategory(true)
			.retrieveType(Bucket.RetrieveType.MATCH)
			.tabIds(Set.of())
			.build()
		).await().indefinitely();

		var patched = sessionFactory.withTransaction((s, t) ->
			bucketService.findByName(s, CREATE_BUCKET_TEST_NAME)
				.call(entity -> Mutiny.fetch(entity.getTabs()))
		).await().indefinitely();

		assertFalse(bucket.getTabs().isEmpty());

		assertTrue(patched.getTabs().isEmpty());

	}

	@Test
	@Order(3)
	void should_update_bucket_with_lists() {

		var bucket = sessionFactory.withTransaction((s, t) ->
			bucketService.findByName(s, CREATE_BUCKET_TEST_NAME)
				.call(entity -> Mutiny.fetch(entity.getTabs()))
		).await().indefinitely();

		var suggestionCategoryIds = suggestionCategoryService
			.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		bucketService.update(bucket.getId(), BucketWithListsDTO.builder()
			.name(CREATE_BUCKET_TEST_NAME)
			.refreshOnDate(false)
			.refreshOnQuery(false)
			.refreshOnTab(false)
			.refreshOnSuggestionCategory(true)
			.suggestionCategoryIds(suggestionCategoryIds)
			.retrieveType(Bucket.RetrieveType.KNN)
			.build()
		).await().indefinitely();

		var updated = sessionFactory.withTransaction((s, t) ->
			bucketService.findByName(s, CREATE_BUCKET_TEST_NAME)
				.call(entity -> Mutiny.fetch(entity.getTabs()))
				.call(entity -> Mutiny.fetch(entity.getSuggestionCategories()))
				.call(entity -> Mutiny.fetch(entity.getDatasources()))
		).await().indefinitely();

		assertTrue(updated.getTabs().isEmpty());
		assertEquals(suggestionCategoryIds.size(), updated.getSuggestionCategories().size());
		assertTrue(updated.getDatasources().isEmpty());
		assertFalse(updated.getRefreshOnDate());
		assertEquals(Bucket.RetrieveType.KNN, updated.getRetrieveType());

	}

	@Test
	@Order(4)
	void should_patch_bucket_with_lists_via_graphql() {

		var bucket = sessionFactory.withTransaction((s, t) ->
			bucketService.findByName(s, CREATE_BUCKET_TEST_NAME)
				.call(entity -> Mutiny.fetch(entity.getTabs()))
				.call(entity -> Mutiny.fetch(entity.getSuggestionCategories()))
				.call(entity -> Mutiny.fetch(entity.getDatasources()))
		).await().indefinitely();

		var datasourceIds = datasourceService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var suggestionCategorieIds = suggestionCategoryService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var query = document(
			operation(
				OperationType.MUTATION,
				field(
					"bucketWithLists",
					args(
						arg("id", bucket.getId()),
						arg("patch", true),
						arg(
							"bucketWithListsDTO",
							inputObject(
								prop("name", CREATE_BUCKET_TEST_NAME),
								prop("refreshOnDate", true),
								prop("refreshOnQuery", true),
								prop("refreshOnTab", true),
								prop("refreshOnSuggestionCategory", true),
								prop("retrieveType", gqlEnum("MATCH")),
								prop("suggestionCategoryIds", suggestionCategorieIds
									.stream()
									.limit(1)
									.collect(Collectors.toSet())
								),
								prop("datasourceIds", datasourceIds),
								prop("tabIds", List.of())
							)
						)
					),
					field(
						"entity",
						field("id"),
						field("name"),
						field(
							"suggestionCategories",
							field(
								"edges",
								field(
									"node",
									field("id")
								)
							)
						)
					),
					field(
						"fieldValidators",
						field("field"),
						field("message")
					)
				)
			)
		);

		try {

			var response = graphQLClient.executeSync(query);

			var jsonObject = response.getData().getJsonObject("bucketWithLists");

			assertFalse(jsonObject.isNull("entity"));
			assertTrue(jsonObject.isNull("fieldValidators"));

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Test
	void should_create_bucket_with_lists_via_graphql() {

		var datasourceIds = datasourceService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var suggestionCategorieIds = suggestionCategoryService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var tabIds = tabService.findAll()
			.await().indefinitely()
			.stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		var query = document(
			operation(
				OperationType.MUTATION,
				field(
					"bucketWithLists",
					args(
						arg(
							"bucketWithListsDTO",
							inputObject(
								prop("name", "Create Bucket Test via gql"),
								prop("refreshOnDate", true),
								prop("refreshOnQuery", true),
								prop("refreshOnTab", true),
								prop("refreshOnSuggestionCategory", true),
								prop("retrieveType", gqlEnum("MATCH"))
								,
								prop("datasourceIds", datasourceIds)
								,
								prop("tabIds", tabIds)
								,
								prop("suggestionCategoryIds", suggestionCategorieIds)
							)
						)
					),
					field(
						"entity",
						field("id"),
						field("name")
					),
					field(
						"fieldValidators",
						field("field"),
						field("message")
					)
				)
			)
		);

		try {

			var response = graphQLClient.executeSync(query);

			System.out.println(response);

			var jsonObject = response.getData().getJsonObject("bucketWithLists");

			assertFalse(jsonObject.isNull("entity"));
			assertTrue(jsonObject.isNull("fieldValidators"));

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}

