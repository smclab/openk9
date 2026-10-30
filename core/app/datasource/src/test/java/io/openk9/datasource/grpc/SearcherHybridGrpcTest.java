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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import jakarta.inject.Inject;

import io.openk9.datasource.EntitiesUtils;
import io.openk9.datasource.index.model.IndexName;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.ResultAclConfig;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.model.dto.base.DataIndexDTO;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.base.ResultAclConfigDTO;
import io.openk9.datasource.model.dto.request.SuggestionCategoryWithDocTypeFieldDTO;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.DatasourceConnectionObjects;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.service.PluginDriverService;
import io.openk9.datasource.service.ResultAclConfigService;
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
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.generic.Requests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
	private static final String JWT_ISSUER = "openk9-gateway";
	private static final String JWT_USER = "hybrid-user";
	private static final String ROLE_AUDITOR = "auditor";
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
	OpenSearchClient openSearchClient;

	@Inject
	PluginDriverService pluginDriverService;

	@Inject
	ResultAclConfigService resultAclConfigService;

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
		EntitiesUtils.createBucket(BUCKET_NAME, bucketService, sessionFactory);

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

		when(embeddingService.embedQuery(anyString(), any(), any()))
			.thenReturn(Uni.createFrom().item(
				new EmbeddingService.QueryVector(QUERY_VECTOR)));

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

		EntitiesUtils.cleanBucket(bucket, bucketService);
		EntitiesUtils.removeEntity(BUCKET_NAME, bucketService, sessionFactory);

		var config = resultAclConfigService.findCurrent()
			.await().indefinitely();

		if (config != null) {
			EntitiesUtils.removeEntity(
				config, resultAclConfigService, sessionFactory);
		}

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
		var search = search(request(false).toBuilder()
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
	void should_score_with_the_must_clauses_of_the_base_query() {
		// action — a text token next to the hybrid one, as the filters of the
		// frontend send, makes the base query score with a must clause
		var search = search(request(false, ROLE_FINANCE).toBuilder()
			.addSearchQuery(SearchTokenRequest.newBuilder()
				.setTokenType("TEXT")
				.addValues("riservato")
				.build())
			.build());

		// assertion — the must clause restricts both branches
		assertEquals(Set.of(FINANCE_ID), search.ids());
	}

	@Test
	void should_filter_a_hybrid_token_without_text() {
		// action — a hybrid token with no value, sent as a filter so that no
		// minimum score applies
		var search = search(QueryParserRequest.newBuilder()
			.setTenantId(TENANT_ID)
			.addRange(0)
			.addRange(10)
			.setJwt(jwt())
			.addSearchQuery(SearchTokenRequest.newBuilder()
				.setTokenType("HYBRID")
				.setFilter(true)
				.build())
			.build());

		// assertion — the ACLs still apply
		assertEquals(Set.of(OWN_ID, OTHER_ID), search.ids());
	}

	@Test
	void should_filter_by_the_acls() {
		// action
		var userIds = search(request(false)).ids();
		var financeIds = search(request(false, ROLE_FINANCE)).ids();

		// assertion
		assertEquals(Set.of(OWN_ID, OTHER_ID), userIds);
		assertEquals(Set.of(OWN_ID, OTHER_ID, FINANCE_ID), financeIds);
	}

	@Test
	void should_filter_a_search_asking_for_obscured_results() {
		configureResultAcl();

		// action — the flag the frontend sends in the obscuring mode
		var search = search(request(true));

		// assertion — filtered, nothing to obscure
		assertEquals(Set.of(OWN_ID, OTHER_ID), search.ids());
		assertFalse(search.parsed().hasResultAcl());
	}

	@Test
	void should_serve_the_pages_after_the_first_one() {
		// action — one hit per page
		var firstPage = search(page(0)).ids();
		var secondPage = search(page(1)).ids();

		// assertion — the second page follows the first one
		assertEquals(1, firstPage.size());
		assertEquals(1, secondPage.size());
		assertFalse(firstPage.containsAll(secondPage));

		var bothPages = new HashSet<>(firstPage);
		bothPages.addAll(secondPage);

		assertEquals(Set.of(OWN_ID, OTHER_ID), bothPages);
	}

	@Test
	void should_count_the_suggestions_of_a_hybrid_search() {
		// action
		var values = suggestionValues(request(false));

		// assertion — only the documents the user reads
		assertEquals(Set.of("pubblico"), values);
	}

	@Test
	void should_filter_the_suggestions_of_a_super_user_asking_for_obscured_results() {
		configureResultAcl();

		// action — a super-user in the obscuring mode, as the frontend asks
		var values = suggestionValues(request(true, ROLE_AUDITOR));

		// assertion — filtered as the hybrid results are
		assertEquals(Set.of("pubblico"), values);
	}

	private Set<String> suggestionValues(QueryParserRequest request) {
		return searcher.suggestionsQueryParser(request.toBuilder()
				.setSuggestionCategoryId(suggestionCategory.getId())
				.build())
			.await().indefinitely()
			.getResultList()
			.stream()
			.map(Suggestions::getValue)
			.collect(Collectors.toSet());
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

	private void configureResultAcl() {
		resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.enabled(true)
				.superUserRoles(List.of(ROLE_AUDITOR))
				.visibleFieldIds(Set.of(title.getId()))
				.build())
			.await().indefinitely();

		// the invalidation by event does not reach the tests
		cache.invalidateAll().await().indefinitely();
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

	private static QueryParserRequest page(int from) {
		return request(false).toBuilder()
			.setRange(0, from)
			.setRange(1, 1)
			.build();
	}

	private static QueryParserRequest request(
		boolean includeObscured, String... userRoles) {

		return QueryParserRequest.newBuilder()
			.setTenantId(TENANT_ID)
			.addRange(0)
			.addRange(10)
			.setJwt(jwt(userRoles))
			.setIncludeObscured(includeObscured)
			.addSearchQuery(SearchTokenRequest.newBuilder()
				.setTokenType("HYBRID")
				.addValues("bilancio")
				.build())
			.build();
	}

	/**
	 * Signs a JWT as the gateway does, with the key the datasource verifies.
	 */
	private static String jwt(String... userRoles) {
		var jwk = gatewayJwk();

		SecretKey key = new SecretKeySpec(
			Base64.getUrlDecoder().decode(jwk.getString("k")), "HmacSHA256");

		return Jwt
			.issuer(JWT_ISSUER)
			.upn(JWT_USER)
			.groups(Set.of(userRoles))
			.jws()
			.keyId(jwk.getString("kid"))
			.algorithm(SignatureAlgorithm.HS256)
			.sign(key);
	}

	private static JsonObject gatewayJwk() {
		try (var input = Thread.currentThread()
			.getContextClassLoader()
			.getResourceAsStream("gateway.jwk")) {

			return new JsonObject(
				new String(input.readAllBytes(), StandardCharsets.UTF_8));
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
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

		var builder = Requests.builder()
			.method(method)
			.endpoint(endpoint)
			.query(query);

		if (json != null) {
			builder.json(json);
		}

		try (var response = openSearchClient.generic().execute(builder.build())) {
			var body = response.getBody()
				.map(content -> content.bodyAsString())
				.orElse("");

			assertTrue(
				response.getStatus() < 300,
				() -> method + " " + endpoint + " failed: " + body);

			return body;
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
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
