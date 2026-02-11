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

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@QuarkusTest
public class HybridQueryParserTest {

	private SearchSourceBuilder searchSourceBuilder;
	private ParserContext parserContext;

	@Inject
	HybridQueryParser hybridQueryParser;

	@BeforeEach
	void setup() {
		Bucket bucket = new Bucket();
		bucket.setName("bucket test");
		TenantWithBucket tenantWithBucket = new TenantWithBucket("1", bucket);

		ParserSearchToken parserSearchToken = ParserSearchToken.builder()
			.tokenType("TEXT")
			.search(true)
			.values(List.of("test values"))
			.build();

		parserContext = ParserContext.builder()
			.tenantWithBucket(tenantWithBucket)
			.tokenTypeGroup(List.of(parserSearchToken))
			.queryParserConfig(new JsonObject())
			.build();

		searchSourceBuilder = new SearchSourceBuilder();
	}

	@Test
	void should_not_throw_null_pointer_exception() {

		assertDoesNotThrow(() -> hybridQueryParser.apply(parserContext, searchSourceBuilder));
	}
}
