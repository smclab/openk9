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

import java.util.List;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.searcher.client.dto.ParserSearchToken;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;

class HybridQueryParserWiringTest {

	private static final String TENANT_ID = "1";
	private static final List<Float> QUERY_VECTOR = List.of(0.1f, 0.2f, 0.3f);

	@Test
	void should_build_the_semantic_component_via_embedQuery_text() {

		// 1. a hybrid parser with a mocked embedding service and acl parser
		var embeddingService = Mockito.mock(EmbeddingService.class);
		Mockito.when(embeddingService.embedQuery(
				Mockito.anyString(), Mockito.any(), Mockito.any()))
			.thenReturn(Uni.createFrom().item(
				new EmbeddingService.QueryVector(QUERY_VECTOR)));

		var aclQueryParser = Mockito.mock(AclQueryParser.class);
		Mockito.when(aclQueryParser.getBoolQuery(Mockito.any()))
			.thenReturn(new BoolQueryBuilder());

		var parser = new HybridQueryParser();
		parser.embeddingService = embeddingService;
		parser.aclQueryParser = aclQueryParser;

		var bucket = new Bucket();
		bucket.setName("bucket test");

		var token = ParserSearchToken.builder()
			.tokenType("HYBRID")
			.values(List.of("hello world"))
			.build();

		var parserContext = ParserContext.builder()
			.tenantWithBucket(new TenantWithBucket(TENANT_ID, bucket))
			.tokenTypeGroup(List.of(token))
			.queryParserConfig(new JsonObject())
			.build();

		// 2. apply the hybrid parser
		parser.apply(parserContext, new SearchSourceBuilder())
			.await().indefinitely();

		// 3. the semantic component is produced from EmbedQuery(text), with
		// no inline media on the hybrid token
		var textCaptor = ArgumentCaptor.forClass(String.class);
		var mediaCaptor =
			ArgumentCaptor.forClass(EmbeddingService.QueryMedia.class);

		Mockito.verify(embeddingService).embedQuery(
			Mockito.eq(TENANT_ID), textCaptor.capture(), mediaCaptor.capture());

		Assertions.assertEquals("hello world", textCaptor.getValue());
		Assertions.assertNull(mediaCaptor.getValue());
	}

}
