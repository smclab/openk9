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

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.searcher.client.dto.ParserSearchToken;

import com.jayway.jsonpath.JsonPath;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensearch.client.opensearch._types.query_dsl.KnnQuery;
import org.opensearch.index.query.BoolQueryBuilder;

class KnnQueryParserTest {

	private static final int VECTOR_SIZE = 1800;
	private static final int K_NEIGHBORS = 2;
	private static final String TENANT_ID = "1";
	private static final List<Float> QUERY_VECTOR = List.of(0.1f, 0.2f, 0.3f);

	@Test
	void should_adds_a_must_clause_with_a_wrapped_knn_query() {

		var parserContext = new ParserContext();

		var boolQueryBuilder = new BoolQueryBuilder();

		parserContext.setMutableQuery(boolQueryBuilder);

		var knnQueryBuilder = new KnnQuery.Builder()
			.field("vector")
			.k(K_NEIGHBORS)
			.vector(randomVector(VECTOR_SIZE))
			.build()
			.toQuery();

		KnnQueryParser.addKnnQuery(parserContext, knnQueryBuilder);

		var query = parserContext.getMutableQuery().toString();

		String wrappedQuery = JsonPath.parse(query).read("$.bool.must[0].wrapper.query");

		var wrappedKnnQuery = new String(Base64.getDecoder().decode(wrappedQuery));

		var documentContext = JsonPath.parse(wrappedKnnQuery);

		int k = documentContext.read("$.knn.vector.k");
		List<Float> vector = documentContext.read("$.knn.vector.vector");

		Assertions.assertEquals(K_NEIGHBORS, k);
		Assertions.assertEquals(VECTOR_SIZE, vector.size());

	}

	@Test
	void should_call_embedQuery_with_text_for_a_text_only_token() {

		// 1. a KNN token with only text values, no media
		var embeddingService = mockEmbeddingService();
		var parser = knnParser(embeddingService);

		var token = ParserSearchToken.builder()
			.tokenType("KNN")
			.values(List.of("hello world"))
			.build();

		var parserContext = knnContext(token);

		// 2. apply the parser
		parser.apply(parserContext).await().indefinitely();

		// 3. EmbedQuery is called with the text and no media
		var textCaptor = ArgumentCaptor.forClass(String.class);
		var mediaCaptor =
			ArgumentCaptor.forClass(EmbeddingService.QueryMedia.class);

		Mockito.verify(embeddingService).embedQuery(
			Mockito.eq(TENANT_ID), textCaptor.capture(), mediaCaptor.capture());

		Assertions.assertEquals("hello world", textCaptor.getValue());
		Assertions.assertNull(mediaCaptor.getValue());

		// and a must clause is added to the mutable query
		Assertions.assertEquals(
			1, parserContext.getMutableQuery().must().size());
	}

	@Test
	void should_call_embedQuery_with_inline_for_a_media_only_token() {

		// 1. a KNN token with only media, no text values
		var embeddingService = mockEmbeddingService();
		var parser = knnParser(embeddingService);

		var imageBytes = new byte[] {1, 2, 3, 4};
		var token = ParserSearchToken.builder()
			.tokenType("KNN")
			.media(new ParserSearchToken.Media(
				Base64.getEncoder().encodeToString(imageBytes), "image/png"))
			.build();

		var parserContext = knnContext(token);

		// 2. apply the parser
		parser.apply(parserContext).await().indefinitely();

		// 3. EmbedQuery is called with no text and the decoded inline media
		var mediaCaptor =
			ArgumentCaptor.forClass(EmbeddingService.QueryMedia.class);

		Mockito.verify(embeddingService).embedQuery(
			Mockito.eq(TENANT_ID), Mockito.isNull(), mediaCaptor.capture());

		var media = mediaCaptor.getValue();
		Assertions.assertNotNull(media);
		Assertions.assertArrayEquals(imageBytes, media.data());
		Assertions.assertEquals("image/png", media.contentType());

		Assertions.assertEquals(
			1, parserContext.getMutableQuery().must().size());
	}

	@Test
	void should_call_embedQuery_with_text_and_inline_for_a_combined_token() {

		// 1. a KNN token with both text values and media
		var embeddingService = mockEmbeddingService();
		var parser = knnParser(embeddingService);

		var imageBytes = new byte[] {9, 8, 7};
		var token = ParserSearchToken.builder()
			.tokenType("KNN")
			.values(List.of("a cat"))
			.media(new ParserSearchToken.Media(
				Base64.getEncoder().encodeToString(imageBytes), "image/jpeg"))
			.build();

		var parserContext = knnContext(token);

		// 2. apply the parser
		parser.apply(parserContext).await().indefinitely();

		// 3. EmbedQuery is called with the text and the inline media together
		var textCaptor = ArgumentCaptor.forClass(String.class);
		var mediaCaptor =
			ArgumentCaptor.forClass(EmbeddingService.QueryMedia.class);

		Mockito.verify(embeddingService).embedQuery(
			Mockito.eq(TENANT_ID), textCaptor.capture(), mediaCaptor.capture());

		Assertions.assertEquals("a cat", textCaptor.getValue());
		var media = mediaCaptor.getValue();
		Assertions.assertNotNull(media);
		Assertions.assertArrayEquals(imageBytes, media.data());
		Assertions.assertEquals("image/jpeg", media.contentType());

		Assertions.assertEquals(
			1, parserContext.getMutableQuery().must().size());
	}

	private EmbeddingService mockEmbeddingService() {
		var embeddingService = Mockito.mock(EmbeddingService.class);
		Mockito.when(embeddingService.embedQuery(
				Mockito.anyString(), Mockito.any(), Mockito.any()))
			.thenReturn(Uni.createFrom().item(
				new EmbeddingService.QueryVector(QUERY_VECTOR)));
		return embeddingService;
	}

	private KnnQueryParser knnParser(EmbeddingService embeddingService) {
		var parser = new KnnQueryParser();
		parser.embeddingService = embeddingService;
		return parser;
	}

	private ParserContext knnContext(ParserSearchToken token) {
		var bucket = new Bucket();
		bucket.setName("bucket test");

		return ParserContext.builder()
			.tenantWithBucket(new TenantWithBucket(TENANT_ID, bucket))
			.tokenTypeGroup(List.of(token))
			.mutableQuery(new BoolQueryBuilder())
			.queryParserConfig(new JsonObject())
			.build();
	}

	private float[] randomVector(int size) {
		var vector = new float[size];

		for (int i = 0; i < size; i++) {

			vector[i] = (float) Math.random();

		}

		return vector;
	}

}
