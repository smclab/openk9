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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.request.BucketWithListsDTO;
import io.openk9.datasource.model.dto.request.SuggestionCategoryWithDocTypeFieldDTO;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.SuggestionCategoryService;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class SuggestionCategoryGraphqlTest {

	private static final String ENTITY_NAME_PREFIX = "SuggestionCategoryGraphqlTest - ";

	private static final String BUCKET_NAME_ONE = ENTITY_NAME_PREFIX + "Bucket 1";
	private static final String DOC_TYPE_FIELD = "docTypeField";
	private static final String DOC_TYPE_FIELD_ID = "docTypeFieldId";
	private static final String EDGES = "edges";
	private static final String ENTITY = "entity";
	private static final String DOC_TYPE_FIELD_ONE_NAME = ENTITY_NAME_PREFIX + "Doc type field 1";
	private static final String DOC_TYPE_FIELD_TWO_NAME = ENTITY_NAME_PREFIX + "Doc type field 2";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String NODE = "node";
	private static final String PATCH = "patch";
	private static final String PRIORITY = "priority";
	private static final String SUGGESTION_CATEGORIES = "suggestionCategories";
	private static final String SUGGESTION_CATEGORY = "suggestionCategory";
	private static final String SUGGESTION_CATEGORY_WITH_DOC_TYPE_FIELD_DTO = "suggestionCategoryWithDocTypeFieldDTO";
	private static final String SUGGESTION_CATEGORY_WITH_DOC_TYPE_FIELD = "suggestionCategoryWithDocTypeField";
	private static final String SUGGESTION_ONE_NAME = ENTITY_NAME_PREFIX + "Suggestion category 1";
	private static final String BUCKETS = "buckets";

	@Inject
	BucketService bucketService;

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	@GraphQLClient("openk9-dynamic")
	DynamicGraphQLClient graphQLClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@BeforeEach
	void setup() {
		Long suggestionCategoriesCount = allSuggestionCategoryCount();

		//create
		createDocTypeFieldOne();
		createDocTypeFieldTwo();
		createSuggestionCategoryOneWithDocTypeFieldOne();
		createBucketOne();

		assertEquals(
			suggestionCategoriesCount + 1,
			allSuggestionCategoryCount());
	}

	@Test
	void should_return_doc_type_field_when_queried_from_suggestion_category()
		throws ExecutionException, InterruptedException {

		var suggestionCategoryOne = getSuggestionCategoryOne();
		var docTypeFieldOne = getDocTypeFieldOne();

		var query = document(
			operation(
				OperationType.QUERY,
				field(
					SUGGESTION_CATEGORY,
					args(
						arg(ID, suggestionCategoryOne.getId())
					),
					field(ID),
					field(NAME),
					field(
						DOC_TYPE_FIELD,
						field(ID),
						field(NAME)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var suggestionCategoryR = response.getData().getJsonObject(SUGGESTION_CATEGORY);

		assertNotNull(suggestionCategoryR);
		assertEquals(
			suggestionCategoryOne.getId(),
			Long.parseLong(suggestionCategoryR.getString(ID)));

		var docTypeFieldR = suggestionCategoryR.getJsonObject(DOC_TYPE_FIELD);

		assertNotNull(docTypeFieldR);
		assertEquals(
			docTypeFieldOne.getId(),
			Long.parseLong(docTypeFieldR.getString(ID)));

	}

	@Test
	void should_return_bucket_when_queried_from_suggestion_category()
		throws ExecutionException, InterruptedException {

		var suggestionCategoryOne = getSuggestionCategoryOne();
		var bucketOne = getBucketOne();

		var query = document(
			operation(
				OperationType.QUERY,
				field(
					SUGGESTION_CATEGORY,
					args(
						arg(ID, suggestionCategoryOne.getId())
					),
					field(ID),
					field(NAME),
					field(
						BUCKETS,
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
		);

		var response = graphQLClient.executeSync(query);

		System.out.println("Response: " + response);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var suggestionCategoryR = response.getData().getJsonObject(SUGGESTION_CATEGORY);

		assertNotNull(suggestionCategoryR);
		assertEquals(
			suggestionCategoryOne.getId(),
			Long.parseLong(suggestionCategoryR.getString(ID)));

		var bucketR = suggestionCategoryR.getJsonObject(BUCKETS).getJsonArray(EDGES)
			.getFirst()
			.asJsonObject()
			.getJsonObject(NODE);

		assertNotNull(bucketR);
		assertEquals(
			bucketOne.getId(),
			Long.parseLong(bucketR.getString(ID)));

	}

	@Test
	void should_patch_suggestion_category_with_doc_type_field()
		throws ExecutionException, InterruptedException {

		var suggestionCategoryOne = getSuggestionCategoryOne();
		var docTypeFieldTwoId = getDocTypeFieldTwo().getId();

		var query = document(
			operation(
				OperationType.MUTATION,
				field(
					SUGGESTION_CATEGORY_WITH_DOC_TYPE_FIELD,
					args(
						arg(ID, suggestionCategoryOne.getId()),
						arg(PATCH, true),
						arg(
							SUGGESTION_CATEGORY_WITH_DOC_TYPE_FIELD_DTO,
							inputObject(
								prop(NAME, SUGGESTION_ONE_NAME),
								prop(DOC_TYPE_FIELD_ID, docTypeFieldTwoId),
								prop(PRIORITY, 100f),
								prop("multiSelect", false)
							)
						)
					),
					field(
						ENTITY,
						field(ID),
						field(NAME),
						field(
							DOC_TYPE_FIELD,
							field(ID),
							field(NAME)
						)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var suggestionCategoryOnePathed = getSuggestionCategoryOne();

		assertEquals(
			getDocTypeFieldTwo().getId(),
			suggestionCategoryOnePathed.getDocTypeField().getId()
		);
		assertEquals(
			DOC_TYPE_FIELD_TWO_NAME,
			suggestionCategoryOnePathed.getDocTypeField().getName()
		);
	}

	@Test
	void should_return_doc_type_field_when_queried_from_all_suggestion_categories()
		throws ExecutionException, InterruptedException {

		var docTypeFieldOne = getDocTypeFieldOne();

		var query = document(
			operation(
				OperationType.QUERY,
				field(
					SUGGESTION_CATEGORIES,
					field(EDGES,
						field(NODE,
							field(ID),
							field(NAME),
							field(
								DOC_TYPE_FIELD,
								field(ID),
								field(NAME)
							)
						)
					)
				)
			)
		);

		var response = graphQLClient.executeSync(query);

		System.out.println("Response: " + response);

		assertFalse(response.hasError());
		assertTrue(response.hasData());

		var suggestionCategoriesR = response.getData()
			.getJsonObject(SUGGESTION_CATEGORIES)
			.getJsonArray(EDGES);

		assertNotNull(suggestionCategoriesR);
		assertEquals(allSuggestionCategoryCount(), suggestionCategoriesR.size());

		System.out.println(suggestionCategoriesR);

		suggestionCategoriesR.getValuesAs(JsonObject.class)
			.forEach(node ->{
				var nodeJsonObject = node.getJsonObject(NODE);
				var name = nodeJsonObject.getString(NAME);

				if (SUGGESTION_ONE_NAME.equalsIgnoreCase(name)) {
					var docTypeField = nodeJsonObject.getJsonObject(DOC_TYPE_FIELD);

					assertNotNull(docTypeField);
					assertEquals(
						docTypeFieldOne.getId(),
						Long.parseLong(docTypeField.getString(ID)));
				}
			});
	}

	@AfterEach
	void tearDown() {
		var docTypeFieldOne = getDocTypeFieldOne();
		var docTypeFieldTwo = getDocTypeFieldTwo();
		var suggestionCategoryOne = getSuggestionCategoryOne();

		unbindBucketOneFromSuggestionCategoryOne();

		suggestionCategoryService.unsetDocTypeField(suggestionCategoryOne.getId())
			.await().indefinitely();

		suggestionCategoryService.deleteById(suggestionCategoryOne.getId())
			.await().indefinitely();

		docTypeFieldService.deleteById(docTypeFieldOne.getId())
			.await().indefinitely();

		docTypeFieldService.deleteById(docTypeFieldTwo.getId())
			.await().indefinitely();

		var bucketOne = getBucketOne();
		bucketService.deleteById(bucketOne.getId()).await().indefinitely();
	}


	private Long allSuggestionCategoryCount() {
		return sessionFactory.withTransaction(
			(s, transaction) ->
				suggestionCategoryService.count()
		)
		.await()
		.indefinitely();
	}

	private void createBucketOne() {
		var suggestionCategoryOneId = getSuggestionCategoryOne().getId();
		var dto = BucketWithListsDTO.builder()
			.name(BUCKET_NAME_ONE)
			.refreshOnDate(true)
			.refreshOnQuery(true)
			.refreshOnTab(true)
			.refreshOnSuggestionCategory(true)
			.retrieveType(Bucket.RetrieveType.TEXT)
			.suggestionCategoryIds(Set.of(suggestionCategoryOneId))
			.build();

		bucketService.create(dto)
			.await().indefinitely();
	}

	private void createDocTypeFieldOne() {
		var dto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_ONE_NAME)
			.fieldName(DOC_TYPE_FIELD_ONE_NAME)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.TEXT)
			.build();

		docTypeFieldService.create(dto)
			.await()
			.indefinitely();
	}

	private void createDocTypeFieldTwo() {
		var dto = DocTypeFieldDTO.builder()
			.name(DOC_TYPE_FIELD_TWO_NAME)
			.fieldName(DOC_TYPE_FIELD_TWO_NAME)
			.searchable(false)
			.sortable(false)
			.fieldType(FieldType.TEXT)
			.build();

		docTypeFieldService.create(dto)
			.await()
			.indefinitely();
	}

	private void createSuggestionCategoryOneWithDocTypeFieldOne() {
		var docTypeFieldId = getDocTypeFieldOne().getId();

		var dto = SuggestionCategoryWithDocTypeFieldDTO.builder()
			.name(SUGGESTION_ONE_NAME)
			.docTypeFieldId(docTypeFieldId)
			.priority(100f)
			.build();

		suggestionCategoryService.create(dto)
			.await()
			.indefinitely();
	}

	private Bucket getBucketOne() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					bucketService.findByName(s, BUCKET_NAME_ONE)
			)
			.await()
			.indefinitely();
	}

	private DocTypeField getDocTypeFieldOne() {
		return sessionFactory.withTransaction(
			(s, transaction) ->
				docTypeFieldService.findByName(s, DOC_TYPE_FIELD_ONE_NAME)
		)
		.await()
		.indefinitely();
	}

	private DocTypeField getDocTypeFieldTwo() {
		return sessionFactory.withTransaction(
				(s, transaction) ->
					docTypeFieldService.findByName(s, DOC_TYPE_FIELD_TWO_NAME)
			)
			.await()
			.indefinitely();
	}

	private SuggestionCategory getSuggestionCategoryOne() {
		return sessionFactory.withTransaction(
			(s, transaction) ->
				suggestionCategoryService.findByName(s, SUGGESTION_ONE_NAME)
					.call(suggestionCategory ->
						Mutiny.fetch(suggestionCategory.getDocTypeField()))
		)
		.await()
		.indefinitely();
	}

	private void unbindBucketOneFromSuggestionCategoryOne() {
		var suggestionCategoryOne = getSuggestionCategoryOne();
		var bucketOne = getBucketOne();

		sessionFactory.withTransaction(
			(s, transaction) ->
				bucketService.removeSuggestionCategory(
					bucketOne.getId(),
					suggestionCategoryOne.getId()
				)
		).await().indefinitely();
	}
}
