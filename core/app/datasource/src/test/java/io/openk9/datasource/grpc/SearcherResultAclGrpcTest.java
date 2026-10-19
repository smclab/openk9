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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import io.openk9.datasource.searcher.parser.impl.AclQueryParser;
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
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
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

/**
 * Runs the query built by the datasource against OpenSearch, as the searcher
 * does, and checks which documents come out and which ones the named query
 * marks as authorized, for a user without roles, a user with a role and a
 * super-user.
 */
@QuarkusTest
public class SearcherResultAclGrpcTest {

	private static final String ENTITY_NAME_PREFIX = "SearcherResultAclGrpcTest - ";
	private static final String BUCKET_NAME = ENTITY_NAME_PREFIX + "bucket";
	private static final String DATASOURCE_NAME = ENTITY_NAME_PREFIX + "datasource";
	private static final String DATA_INDEX_NAME = ENTITY_NAME_PREFIX + "data index";
	private static final String DOC_TYPE_NAME = ENTITY_NAME_PREFIX + "doc type";
	private static final String PLUGIN_DRIVER_NAME =
		ENTITY_NAME_PREFIX + "plugin driver";
	private static final String SUGGESTION_CATEGORY_NAME =
		ENTITY_NAME_PREFIX + "suggestion category";

	private static final String TENANT_ID = "public";
	private static final String JWT_ISSUER = "openk9-gateway";
	private static final String JWT_USER = "result-acl-user";
	private static final String ROLE_AUDITOR = "auditor";
	private static final String ROLE_FINANCE = "finance";
	private static final String ROLE_LEGAL = "legal";

	private static final String PUBLIC_ID = "public-report";
	private static final String FINANCE_ID = "finance-merger";
	private static final String LEGAL_ID = "legal-dispute";

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
	private SuggestionCategory suggestionCategory;
	private Datasource datasource;

	@BeforeEach
	void setup() {
		// 1. the fields: title and body searchable, the roles of the ACL and
		// a keyword for the filters
		docType = docTypeService.create(DocTypeDTO.builder()
				.name(DOC_TYPE_NAME)
				.build())
			.await().indefinitely();

		title = addField("title", FieldType.TEXT, true);
		var body = addField("body", FieldType.TEXT, true);
		roles = addField("roles", FieldType.KEYWORD, false);
		var category = addField("category", FieldType.KEYWORD, false);

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

		// 6. a public document, one for finance and one for legal
		createIndex(body, category);

		indexDocument(PUBLIC_ID, true, List.of(), Map.of(
			title, "Bilancio annuale",
			body, "Relazione aperta a tutti",
			category, "pubblico"));
		indexDocument(FINANCE_ID, false, List.of(ROLE_FINANCE), Map.of(
			title, "Fusione con Alfa",
			body, "Accordo riservato del consiglio",
			category, "finanza"));
		indexDocument(LEGAL_ID, false, List.of(ROLE_LEGAL), Map.of(
			title, "Contenzioso con Beta",
			body, "Causa in corso",
			category, "legale"));

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

		cache.invalidateAll().await().indefinitely();
	}

	@Test
	void should_filter_when_the_mode_is_disabled() {
		configure(false);

		// action
		var search = search(request(true));

		// assertion — only the public document, nothing to obscure
		assertEquals(Set.of(PUBLIC_ID), search.ids());
		assertFalse(search.parsed().hasResultAcl());
	}

	@Test
	void should_filter_a_search_not_asking_for_obscured_results() {
		configure(true);

		// action — a search without the flag, as the RAG does
		var search = search(request(false));

		// assertion
		assertEquals(Set.of(PUBLIC_ID), search.ids());
		assertFalse(search.parsed().hasResultAcl());
	}

	@Test
	void should_mark_only_the_public_result_for_a_user_without_roles() {
		configure(true);

		// action
		var search = search(request(true));

		// assertion — every document comes out, only the public one authorized
		assertEquals(Set.of(PUBLIC_ID, FINANCE_ID, LEGAL_ID), search.ids());
		assertEquals(Set.of(PUBLIC_ID), search.authorizedIds());

		var resultAcl = search.parsed().getResultAcl();

		assertEquals(
			AclQueryParser.AUTHORIZED_QUERY_NAME,
			resultAcl.getAuthorizedQueryName());
		assertEquals(
			List.of(title.getPath()), resultAcl.getVisibleFieldsList());
	}

	@Test
	void should_mark_the_results_a_role_authorizes() {
		configure(true);

		// action
		var search = search(request(true, ROLE_FINANCE));

		// assertion
		assertEquals(Set.of(PUBLIC_ID, FINANCE_ID, LEGAL_ID), search.ids());
		assertEquals(Set.of(PUBLIC_ID, FINANCE_ID), search.authorizedIds());
	}

	@Test
	void should_return_every_result_in_full_to_a_super_user() {
		configure(true);

		// action
		var search = search(request(true, ROLE_AUDITOR));

		// assertion — no ACL clause, so no named query and nothing to obscure
		assertEquals(Set.of(PUBLIC_ID, FINANCE_ID, LEGAL_ID), search.ids());
		assertEquals(Set.of(), search.authorizedIds());
		assertFalse(search.parsed().hasResultAcl());
		assertFalse(search.parsed().getQuery().toStringUtf8().contains("acl.public"));
	}

	@Test
	void should_filter_a_super_user_when_the_mode_is_disabled() {
		configure(false);

		// action
		var search = search(request(true, ROLE_AUDITOR));

		// assertion
		assertEquals(Set.of(PUBLIC_ID), search.ids());
	}

	@Test
	void should_count_only_readable_documents_in_the_filters() {
		configure(true);

		// action
		var userValues = suggestionValues(request(true));
		var superUserValues = suggestionValues(request(true, ROLE_AUDITOR));

		// assertion — a super-user counts every document
		assertEquals(Set.of("pubblico"), userValues);
		assertEquals(Set.of("pubblico", "finanza", "legale"), superUserValues);
	}

	@Test
	void should_return_an_obscured_result_found_by_a_hidden_field() {
		configure(true);

		// action — a text found only in the body of the finance document
		var search = search(request(true).toBuilder()
			.addSearchQuery(SearchTokenRequest.newBuilder()
				.setTokenType("TEXT")
				.addValues("riservato")
				.build())
			.build());

		// assertion — it comes out and counts in the total, not authorized
		assertEquals(Set.of(FINANCE_ID), search.ids());
		assertEquals(1, search.total());
		assertEquals(Set.of(), search.authorizedIds());
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

	private void configure(boolean enabled) {
		resultAclConfigService.create(ResultAclConfigDTO.builder()
				.name(ResultAclConfig.DEFAULT_NAME)
				.enabled(enabled)
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

	private void createIndex(DocTypeField body, DocTypeField category) {
		var docTypeProperties = new JsonObject();

		for (var field : List.of(title, body, roles, category)) {
			docTypeProperties.put(
				field.getFieldName(),
				new JsonObject().put(
					"type",
					field.getFieldType() == FieldType.TEXT ? "text" : "keyword"));
		}

		var mappings = new JsonObject().put("properties", new JsonObject()
			.put("acl", new JsonObject().put("properties", new JsonObject()
				.put("public", new JsonObject().put("type", "boolean"))))
			.put(DOC_TYPE_NAME, new JsonObject()
				.put("properties", docTypeProperties)));

		send("PUT", "/" + indexName(), Map.of(),
			new JsonObject().put("mappings", mappings).encode());
	}

	private void indexDocument(
		String id, boolean isPublic, List<String> documentRoles,
		Map<DocTypeField, String> values) {

		var docTypeValues = new JsonObject()
			.put(roles.getFieldName(), new JsonArray(documentRoles));

		values.forEach((field, value) ->
			docTypeValues.put(field.getFieldName(), value));

		var document = new JsonObject()
			.put("acl", new JsonObject().put("public", isPublic))
			.put(DOC_TYPE_NAME, docTypeValues);

		send("PUT", "/" + indexName() + "/_doc/" + id,
			Map.of("refresh", "true"), document.encode());
	}

	private String indexName() {
		var dataIndex = datasourceService.getDataIndex(datasource.getId())
			.await().indefinitely();

		return IndexName.from(TENANT_ID, dataIndex).toString();
	}

	private static QueryParserRequest request(
		boolean includeObscured, String... userRoles) {

		return QueryParserRequest.newBuilder()
			.setTenantId(TENANT_ID)
			.addRange(0)
			.addRange(10)
			.setJwt(jwt(userRoles))
			.setIncludeObscured(includeObscured)
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
	 * OpenSearch, on the indices and with the parameters the datasource
	 * returns, as the searcher does.
	 */
	private Search search(QueryParserRequest request) {
		var parsed = searcher.queryParser(request).await().indefinitely();

		var response = send(
			"GET",
			"/" + String.join(",", parsed.getIndexNameList()) + "/_search",
			parsed.getQueryParametersMap(),
			parsed.getQuery().toStringUtf8());

		return new Search(parsed, new JsonObject(response).getJsonObject("hits"));
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

	private String send(
		String method, String endpoint, Map<String, String> query, String json) {

		var request = Requests.builder()
			.method(method)
			.endpoint(endpoint)
			.query(query)
			.json(json)
			.build();

		try (var response = openSearchClient.generic().execute(request)) {
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
			return hitStream()
				.map(hit -> hit.getString("_id"))
				.collect(Collectors.toSet());
		}

		Set<String> authorizedIds() {
			var authorized = new HashSet<String>();

			hitStream().forEach(hit -> {
				var matchedQueries = hit.getJsonArray("matched_queries");

				if (matchedQueries != null && matchedQueries.contains(
					AclQueryParser.AUTHORIZED_QUERY_NAME)) {

					authorized.add(hit.getString("_id"));
				}
			});

			return authorized;
		}

		long total() {
			return hits.getJsonObject("total").getLong("value");
		}

		private Stream<JsonObject> hitStream() {
			return hits.getJsonArray("hits").stream().map(JsonObject.class::cast);
		}

	}

}
