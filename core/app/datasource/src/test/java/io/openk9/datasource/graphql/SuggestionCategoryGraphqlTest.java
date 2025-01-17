package io.openk9.datasource.graphql;

import io.openk9.datasource.graphql.dto.BucketWithListsDTO;
import io.openk9.datasource.graphql.dto.SuggestionCategoryWithDocTypeFieldDTO;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.SuggestionCategoryService;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Set;
import java.util.concurrent.ExecutionException;

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

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SuggestionCategoryGraphqlTest {

	private static final String ENTITY_NAME_PREFIX = "SuggestionCategoryGraphqlTest - ";

	private static final String BUCKET = "bucket";
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

	@Test
	@Order(1)
	void setup() {
		Long suggestionCategoriesCount = allSuggestionCategoryCount();

		//create
		createDocTypeFieldOne();
		createDocTypeFieldTwo();
		createSuggestionCategoryOneWithDocTypeFieldOne();
		createBucketOne();

		//bind
		bindBucketOneToSuggestionCategoryOne();

		assertEquals(
			suggestionCategoriesCount + 1,
			allSuggestionCategoryCount());
	}

	@Test
	@Order(2)
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
	@Order(3)
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
						BUCKET,
						field(ID),
						field(NAME)
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

		var bucketR = suggestionCategoryR.getJsonObject(BUCKET);

		assertNotNull(bucketR);
		assertEquals(
			bucketOne.getId(),
			Long.parseLong(bucketR.getString(ID)));

	}

	@Test
	@Order(4)
	void should_patch_suggestion_category_with_doc_type_field() throws ExecutionException, InterruptedException {
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
					field(ENTITY,
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
			suggestionCategoryOnePathed.getDocTypeField().getId());
		assertEquals(
			DOC_TYPE_FIELD_TWO_NAME,
			suggestionCategoryOnePathed.getDocTypeField().getName());
	}

	@Test
	@Order(5)
	void should_return_doc_type_field_when_queried_from_all_suggestion_categories()
		throws ExecutionException, InterruptedException {

		var docTypeFieldTwo = getDocTypeFieldTwo();

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
						docTypeFieldTwo.getId(),
						Long.parseLong(docTypeField.getString(ID)));
				}
			});
	}

	@Test
	@Order(6)
	void tearDown() {
		var docTypeFieldOne = getDocTypeFieldOne();
		var docTypeFieldTwo = getDocTypeFieldTwo();
		var suggestionCategoryOne = getSuggestionCategoryOne();

		suggestionCategoryService.unsetDocTypeField(suggestionCategoryOne.getId())
			.await().indefinitely();

		suggestionCategoryService.deleteById(suggestionCategoryOne.getId())
			.await().indefinitely();

		docTypeFieldService.deleteById(docTypeFieldOne.getId())
			.await().indefinitely();

		docTypeFieldService.deleteById(docTypeFieldTwo.getId())
			.await().indefinitely();
	}

	private Long allSuggestionCategoryCount() {
		return sessionFactory.withTransaction(
			(s, transaction) ->
				suggestionCategoryService.count()
		)
		.await()
		.indefinitely();
	}

	private void bindBucketOneToSuggestionCategoryOne() {
		var suggestionCategoryOne = getSuggestionCategoryOne();
		var bucketOne = getBucketOne();

		sessionFactory.withTransaction(
			(s, transaction) ->
				bucketService.addSuggestionCategory(bucketOne.getId(), suggestionCategoryOne.getId())
		);
	}

	private void createBucketOne() {
		var suggestionCategoryOneId = getSuggestionCategoryOne().getId();
		var dto = BucketWithListsDTO.builder()
			.name(BUCKET_NAME_ONE)
			.refreshOnDate(true)
			.refreshOnQuery(true)
			.refreshOnTab(true)
			.refreshOnSuggestionCategory(true)
			.retrieveType(Bucket.RetrieveType.MATCH)
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
}
