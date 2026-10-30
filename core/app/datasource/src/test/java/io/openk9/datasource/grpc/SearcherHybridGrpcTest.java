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

package io.openk9.datasource.grpc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.index.model.IndexName;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.model.dto.base.BucketDTO;
import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.request.SuggestionCategoryWithDocTypeFieldDTO;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.DatasourceConnectionObjects;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.service.PluginDriverService;
import io.openk9.datasource.service.SuggestionCategoryService;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.QueryParserResponse;
import io.openk9.searcher.grpc.SearchTokenRequest;
import io.openk9.searcher.grpc.Searcher;
import io.openk9.searcher.grpc.Suggestions;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.client.Request;
import org.opensearch.client.RestHighLevelClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Runs the hybrid query built by the datasource against OpenSearch, as the
 * searcher does, and checks that the filters and the ACLs of the request
 * restrict both of its branches.
 */
@QuarkusTest
public class SearcherHybridGrpcTest {

	private static final String ENTITY_NAME_PREFIX = "SearcherHybridGrpcTest - ";
	private static final String BUCKET_NAME = ENTITY_NAME_PREFIX + "bucket";
	private static final String DATASOURCE_NAME = ENTITY_NAME_PREFIX + "datasource";
	private static final String DATA_INDEX_NAME = ENTITY_NAME_PREFIX + "data index";
	private static final String DOC_TYPE_NAME = ENTITY_NAME_PREFIX + "doc type";
	private static final String PLUGIN_DRIVER_NAME =
		ENTITY_NAME_PREFIX + "plugin driver";
	private static final String SUGGESTION_CATEGORY_NAME =
		ENTITY_NAME_PREFIX + "suggestion category";
	private static final String SEARCH_PIPELINE = "searcherhybridgrpctest";

	private static final String TENANT_ID = "public";
	private static final String VIRTUAL_HOST = "test.openk9.local";
	private static final String JWT_USER = "hybrid-user";
	private static final String ROLE_FINANCE = "finance";

	private static final List<Float> QUERY_VECTOR = List.of(1f, 0f, 0f);
	private static final long OTHER_DATASOURCE_ID = 999_999L;

	private static final String OWN_ID = "own-report";
	private static final String OTHER_ID = "other-report";
	private static final String FINANCE_ID = "finance-report";

	@GrpcClient
	Searcher searcher;

	@Inject
	BucketService bucketService;

	@Inject
	@CacheName("searcher-service")
	Cache cache;

	@Inject
	DatasourceService datasourceService;

	@Inject
	DocTypeService docTypeService;

	@InjectMock
	EmbeddingService embeddingService;

	@Inject
	RestHighLevelClient openSearchClient;

	@Inject
	PluginDriverService pluginDriverService;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	SuggestionCategoryService suggestionCategoryService;

	private DocType docType;
	private DocTypeField title;
	private DocTypeField roles;
	private DocTypeField category;
	private SuggestionCategory suggestionCategory;
	private Datasource datasource;

	@BeforeEach
	void setup() {
		// 1. the fields: a searchable title, the roles of the ACL and a
		// keyword for the filters
		docType = docTypeService.create(DocTypeDTO.builder()
				.name(DOC_TYPE_NAME)
				.build())
			.await().indefinitely();

		title = addField("title", FieldType.TEXT, true);
		roles = addField("roles", FieldType.KEYWORD, false);
		category = addField("category", FieldType.KEYWORD, false);

		// 2. a plugin driver mapping the roles of the JWT to the roles field
		var pluginDriver = pluginDriverService.create(
				DatasourceConnectionObjects.PLUGIN_DRIVER_DTO_BUILDER()
					.name(PLUGIN_DRIVER_NAME)
					.build())
			.await().indefinitely();

		pluginDriverService.addDocTypeField(
				pluginDriver.getId(), roles.getId(), UserField.ROLES)
			.await().indefinitely();

		// 3. a datasource indexing the doc type
		datasource = datasourceService.createDatasourceConnection(
				DatasourceConnectionObjects.DATASOURCE_CONNECTION_DTO_BUILDER()
					.name(DATASOURCE_NAME)
					.schedulable(false)
					.pluginDriverId(pluginDriver.getId())
					.dataIndex(DataIndexDTO.builder()
						.name(DATA_INDEX_NAME)
						.knnIndex(false)
						.docTypeIds(Set.of(docType.getId()))
						.build())
					.build())
			.await().indefinitely()
			.getEntity();

		// 4. a filter on the category
		suggestionCategory = suggestionCategoryService.create(
				SuggestionCategoryWithDocTypeFieldDTO.builder()
					.name(SUGGESTION_CATEGORY_NAME)
					.docTypeFieldId(category.getId())
					.priority(1f)
					.multiSelect(false)
					.build())
			.await().indefinitely();

		// 5. the bucket searched by the tenant
		EntitiesUtils.createEntity(
			BucketDTO.builder()
				.name(BUCKET_NAME)
				.refreshOnSuggestionCategory(false)
				.refreshOnTab(false)
				.refreshOnDate(false)
				.refreshOnQuery(false)
				.retrieveType(Bucket.RetrieveType.TEXT)
				.build(),
			bucketService,
			sessionFactory
		);

		var bucket = EntitiesUtils.getEntity(
			BUCKET_NAME, bucketService, sessionFactory);

		bucketService.addDatasource(bucket.getId(), datasource.getId())
			.await().indefinitely();
		bucketService.addSuggestionCategory(
				bucket.getId(), suggestionCategory.getId())
			.await().indefinitely();
		enableBucket(bucket);

		// 6. three chunks with the same vector: a public one of the
		// datasource, a public one of another datasource and one for finance
		createIndex();
		createSearchPipeline();

		indexDocument(OWN_ID, datasource.getId(), true, List.of(),
			"Bilancio annuale", "pubblico");
		indexDocument(OTHER_ID, OTHER_DATASOURCE_ID, true, List.of(),
			"Bilancio trimestrale", "pubblico");
		indexDocument(FINANCE_ID, datasource.getId(), false, List.of(ROLE_FINANCE),
			"Bilancio riservato", "finanza");

		when(embeddingService.getEmbeddedText(anyString(), anyString()))
			.thenReturn(Uni.createFrom().item(
				new EmbeddingService.EmbeddedText(QUERY_VECTOR)));

		cache.invalidateAll().await().indefinitely();
	}

	@AfterEach
	void tearDown() {
		// the default bucket goes back to the tenant
		var defaultBucket = bucketService
			.findByName(TENANT_ID, io.openk9.datasource.model.init.Bucket.INSTANCE.getName())
			.await().indefinitely();

		enableBucket(defaultBucket);

		var bucket = EntitiesUtils.getEntity(
			BUCKET_NAME, bucketService, sessionFactory);

		EntitiesUtils.cleanBucket(bucketService, bucket);
		EntitiesUtils.removeEntity(BUCKET_NAME, bucketService, sessionFactory);

		suggestionCategoryService.unsetDocTypeField(suggestionCategory.getId())
			.await().indefinitely();
		suggestionCategoryService.deleteById(suggestionCategory.getId())
			.await().indefinitely();

		// also deletes the data index and its OpenSearch index
		datasourceService.deleteById(datasource.getId())
			.await().indefinitely();

		EntitiesUtils.removeEntity(
			PLUGIN_DRIVER_NAME, pluginDriverService, sessionFactory);

		docTypeService.deleteById(docType.getId()).await().indefinitely();

		send("DELETE", "/_search/pipeline/" + SEARCH_PIPELINE, Map.of(), null);

		cache.invalidateAll().await().indefinitely();
	}

	@Test
	void should_apply_the_datasource_filter() {
		// action — a hybrid search restricted to the datasource
		var search = search(request().toBuilder()
			.addSearchQuery(SearchTokenRequest.newBuilder()
				.setTokenType("DATASOURCE")
				.addValues(String.valueOf(datasource.getId()))
				.setFilter(true)
				.build())
			.build());

		// assertion — the chunk of the other datasource does not come out,
		// from either branch
		assertEquals(Set.of(OWN_ID), search.ids());
	}

	@Test
	void should_filter_by_the_acls() {
		// action
		var userIds = search(request()).ids();
		var financeIds = search(request(ROLE_FINANCE)).ids();

		// assertion
		assertEquals(Set.of(OWN_ID, OTHER_ID), userIds);
		assertEquals(Set.of(OWN_ID, OTHER_ID, FINANCE_ID), financeIds);
	}

	@Test
	void should_count_the_suggestions_of_a_hybrid_search() {
		// action
		var values = searcher.suggestionsQueryParser(request().toBuilder()
				.setSuggestionCategoryId(suggestionCategory.getId())
				.build())
			.await().indefinitely()
			.getResultList()
			.stream()
			.map(Suggestions::getValue)
			.collect(Collectors.toSet());

		// assertion — only the documents the user reads
		assertEquals(Set.of("pubblico"), values);
	}

	private DocTypeField addField(
		String fieldName, FieldType fieldType, boolean searchable) {

		return docTypeService.addDocTypeField(
				docType.getId(),
				DocTypeFieldDTO.builder()
					.name(ENTITY_NAME_PREFIX + fieldName)
					.fieldName(fieldName)
					.fieldType(fieldType)
					.searchable(searchable)
					.build())
			.await().indefinitely()
			.right;
	}

	private void enableBucket(Bucket bucket) {
		sessionFactory.withTransaction(
				session -> bucketService.enableTenant(session, bucket.getId()))
			.await().indefinitely();
	}

	private void createIndex() {
		var docTypeProperties = new JsonObject()
			.put(title.getFieldName(), new JsonObject().put("type", "text"))
			.put(roles.getFieldName(), new JsonObject().put("type", "keyword"))
			.put(category.getFieldName(), new JsonObject().put("type", "keyword"));

		var vector = new JsonObject()
			.put("type", "knn_vector")
			.put("dimension", QUERY_VECTOR.size())
			.put("method", new JsonObject()
				.put("name", "hnsw")
				.put("engine", "faiss")
				.put("space_type", "l2"));

		var mappings = new JsonObject().put("properties", new JsonObject()
			.put("acl", new JsonObject().put("properties", new JsonObject()
				.put("public", new JsonObject().put("type", "boolean"))))
			.put("datasourceId", new JsonObject().put("type", "long"))
			.put("chunkText", new JsonObject().put("type", "text"))
			.put("vector", vector)
			.put(DOC_TYPE_NAME, new JsonObject()
				.put("properties", docTypeProperties)));

		var settings = new JsonObject().put("index.knn", true);

		send("PUT", "/" + indexName(), Map.of(), new JsonObject()
			.put("settings", settings)
			.put("mappings", mappings)
			.encode());
	}

	/**
	 * Creates the normalization pipeline a search config creates, which
	 * OpenSearch needs to combine the scores of the two branches.
	 */
	private void createSearchPipeline() {
		var normalization = new JsonObject()
			.put("normalization", new JsonObject().put("technique", "min_max"))
			.put("combination", new JsonObject().put("technique", "arithmetic_mean"));

		var pipeline = new JsonObject().put(
			"phase_results_processors",
			new JsonArray().add(new JsonObject()
				.put("normalization-processor", normalization)));

		send("PUT", "/_search/pipeline/" + SEARCH_PIPELINE, Map.of(),
			pipeline.encode());
	}

	private void indexDocument(
		String id, long datasourceId, boolean isPublic, List<String> documentRoles,
		String titleValue, String categoryValue) {

		var docTypeValues = new JsonObject()
			.put(title.getFieldName(), titleValue)
			.put(roles.getFieldName(), new JsonArray(documentRoles))
			.put(category.getFieldName(), categoryValue);

		var document = new JsonObject()
			.put("acl", new JsonObject().put("public", isPublic))
			.put("datasourceId", datasourceId)
			.put("chunkText", titleValue)
			.put("vector", new JsonArray(QUERY_VECTOR))
			.put(DOC_TYPE_NAME, docTypeValues);

		send("PUT", "/" + indexName() + "/_doc/" + id,
			Map.of("refresh", "true"), document.encode());
	}

	private String indexName() {
		var dataIndex = datasourceService.getDataIndex(datasource.getId())
			.await().indefinitely();

		return IndexName.from(TENANT_ID, dataIndex).toString();
	}

	private static QueryParserRequest request(String... userRoles) {

		return QueryParserRequest.newBuilder()
			.setVirtualHost(VIRTUAL_HOST)
			.addRange(0)
			.addRange(10)
			.setJwt(jwt(userRoles))
			.addSearchQuery(SearchTokenRequest.newBuilder()
				.setTokenType("HYBRID")
				.addValues("bilancio")
				.build())
			.build();
	}

	/**
	 * Builds a JWT as the gateway forwards it: the datasource reads the
	 * claims of the payload without verifying the signature.
	 */
	private static String jwt(String... userRoles) {
		var payload = new JsonObject()
			.put("preferred_username", JWT_USER)
			.put("realm_access", new JsonObject()
				.put("roles", new JsonArray(List.of(userRoles))));

		return encode(new JsonObject().put("alg", "none"))
			+ "." + encode(payload) + ".";
	}

	private static String encode(JsonObject json) {
		return Base64.getEncoder()
			.withoutPadding()
			.encodeToString(json.encode().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Parses the request with the datasource and sends the query to
	 * OpenSearch, on the indices the datasource returns and through the
	 * normalization pipeline, as the searcher does.
	 */
	private Search search(QueryParserRequest request) {
		var parsed = searcher.queryParser(request).await().indefinitely();

		var queryParameters = new HashMap<>(parsed.getQueryParametersMap());
		queryParameters.put("search_pipeline", SEARCH_PIPELINE);

		var response = send(
			"GET",
			"/" + String.join(",", parsed.getIndexNameList()) + "/_search",
			queryParameters,
			parsed.getQuery().toStringUtf8());

		return new Search(parsed, new JsonObject(response).getJsonObject("hits"));
	}

	private String send(
		String method, String endpoint, Map<String, String> query, String json) {

		var request = new Request(method, endpoint);

		request.addParameters(query);

		if (json != null) {
			request.setJsonEntity(json);
		}

		try {
			var response = openSearchClient
				.getLowLevelClient()
				.performRequest(request);

			var entity = response.getEntity();

			return entity == null
				? ""
				: new String(
					entity.getContent().readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new UncheckedIOException(method + " " + endpoint + " failed", e);
		}
	}

	private record Search(QueryParserResponse parsed, JsonObject hits) {

		Set<String> ids() {
			return hits.getJsonArray("hits").stream()
				.map(JsonObject.class::cast)
				.map(hit -> hit.getString("_id"))
				.collect(Collectors.toSet());
		}

	}

}
