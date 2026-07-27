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

package io.openk9.datasource.searcher.parser.impl;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.searcher.client.dto.ParserSearchToken;

import com.jayway.jsonpath.JsonPath;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.search.builder.SearchSourceBuilder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
public class HybridQueryParserTest {

	private static final String TENANT_ID = "1";
	private static final float TITLE_BOOST = 5.0f;
	private static final float CONTENT_BOOST = 1.0f;
	private static final int K_NEIGHBORS = 3;
	private static final List<Float> VECTOR = List.of(0.1f, 0.2f, 0.3f);

	private SearchSourceBuilder searchSourceBuilder;
	private Bucket bucket;
	private ParserSearchToken parserSearchToken;

	@Inject
	HybridQueryParser hybridQueryParser;

	@InjectMock
	EmbeddingService embeddingService;

	@BeforeEach
	void setup() {
		bucket = new Bucket();
		bucket.setName("bucket test");

		parserSearchToken = ParserSearchToken.builder()
			.tokenType("TEXT")
			.search(true)
			.values(List.of("test values"))
			.extra(Map.of("kNeighbors", String.valueOf(K_NEIGHBORS)))
			.build();

		searchSourceBuilder = new SearchSourceBuilder();

		when(embeddingService.getEmbeddedText(anyString(), anyString()))
			.thenReturn(Uni.createFrom().item(
				new EmbeddingService.EmbeddedText(VECTOR)));
	}

	@Test
	void should_not_throw_null_pointer_exception() {

		assertDoesNotThrow(() -> hybridQueryParser.apply(
			createParserContext(), searchSourceBuilder));
	}

	@Test
	void should_inject_acl_query_parser_after_refactoring() {
		// after refactoring, AclQueryParser is injected via CDI
		assertNotNull(hybridQueryParser.getAclQueryParser());

		// and getBoolQuery should not throw with default config
		assertDoesNotThrow(() -> hybridQueryParser.getAclQueryParser()
			.getBoolQuery(createParserContext()));
	}

	@Test
	void should_build_multi_match_with_searchable_fields() {
		// a bucket exposing both the title and the content as searchable text
		withSearchableFields();

		var hybridQuery = applyAndGetHybridQuery();

		// the textual branch is a multi_match, not a match on chunkText only
		assertFalse(hybridQuery.contains("\"match\""));

		assertEquals(
			"most_fields",
			readString(hybridQuery, "$.hybrid.queries[0].multi_match.type"));

		List<String> fields = readList(
			hybridQuery, "$.hybrid.queries[0].multi_match.fields");

		// chunkText is always searched, together with the searchable fields
		assertTrue(fields.contains("chunkText"));
		assertTrue(fields.contains("web.content^" + CONTENT_BOOST));
	}

	@Test
	void should_propagate_doc_type_field_boost() {
		// web.title is configured with a boost higher than the default one
		withSearchableFields();

		var hybridQuery = applyAndGetHybridQuery();

		List<String> fields = readList(
			hybridQuery, "$.hybrid.queries[0].multi_match.fields");

		// the configured boost drives the weight of the title at query time
		assertTrue(fields.contains("web.title^" + TITLE_BOOST));
	}

	@Test
	void should_fallback_to_chunk_text_match() {
		// no searchable text field is configured on the bucket
		var hybridQuery = applyAndGetHybridQuery();

		// the textual branch falls back to the plain match on chunkText
		assertFalse(hybridQuery.contains("multi_match"));
		assertNotNull(
			readString(hybridQuery, "$.hybrid.queries[0].match.chunkText.query"));
	}

	@Test
	void should_keep_knn_query_unchanged() {
		withSearchableFields();

		var hybridQuery = applyAndGetHybridQuery();

		// the knn branch is untouched by the textual branch changes
		assertEquals(
			K_NEIGHBORS,
			JsonPath.parse(hybridQuery)
				.read("$.hybrid.queries[1].knn.vector.k", Integer.class));

		List<Number> vector = readList(
			hybridQuery, "$.hybrid.queries[1].knn.vector.vector");

		assertEquals(VECTOR.size(), vector.size());
		assertEquals(VECTOR.getFirst(), vector.getFirst().floatValue());
	}

	private void withSearchableFields() {
		var docType = new DocType();
		docType.setName("web");

		var title = createDocTypeField(docType, "title", TITLE_BOOST);
		var content = createDocTypeField(docType, "content", CONTENT_BOOST);

		docType.setDocTypeFields(Set.of(title, content));

		var dataIndex = new DataIndex();
		dataIndex.setName("test-index");
		dataIndex.setDocTypes(Set.of(docType));

		var pluginDriver = new PluginDriver();
		pluginDriver.setAclMappings(Set.of());

		var datasource = new Datasource();
		datasource.setDataIndex(dataIndex);
		datasource.setPluginDriver(pluginDriver);

		bucket.setDatasources(Set.of(datasource));
	}

	private static DocTypeField createDocTypeField(
		DocType docType, String fieldName, float boost) {

		var docTypeField = new DocTypeField();
		docTypeField.setDocType(docType);
		docTypeField.setFieldName(fieldName);
		docTypeField.setFieldType(FieldType.TEXT);
		docTypeField.setSearchable(true);
		docTypeField.setBoost((double) boost);

		return docTypeField;
	}

	private ParserContext createParserContext() {
		return ParserContext.builder()
			.tenantWithBucket(new TenantWithBucket(TENANT_ID, bucket))
			.tokenTypeGroup(List.of(parserSearchToken))
			.queryParserConfig(new JsonObject())
			.language(Language.NONE)
			.build();
	}

	/**
	 * Applies the parser and returns the hybrid query as a json string,
	 * unwrapped from the base64 wrapper query set on the SearchSourceBuilder.
	 */
	private String applyAndGetHybridQuery() {
		var searchSource = hybridQueryParser
			.apply(createParserContext(), searchSourceBuilder)
			.await().indefinitely();

		String wrappedQuery = JsonPath
			.parse(searchSource.query().toString())
			.read("$.wrapper.query");

		return new String(Base64.getDecoder().decode(wrappedQuery));
	}

	private static String readString(String json, String path) {
		return JsonPath.parse(json).read(path, String.class);
	}

	private static <T> List<T> readList(String json, String path) {
		return JsonPath.parse(json).read(path);
	}
}
