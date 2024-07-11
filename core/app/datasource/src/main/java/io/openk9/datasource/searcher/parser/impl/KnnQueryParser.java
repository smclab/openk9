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

import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.smallrye.mutiny.Uni;
import jakarta.json.Json;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch._types.query_dsl.KnnQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.index.query.QueryBuilders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class KnnQueryParser implements QueryParser {

	@Inject
	EmbeddingService embeddingService;

	@Override
	public String getType() {
		return "KNN";
	}

	@Override
	public Uni<Void> apply(ParserContext parserContext) {

		var queryParserConfig = parserContext.getQueryParserConfig();

		var tokenTypeGroup = parserContext.getTokenTypeGroup();

		var currentTenant = parserContext.getCurrentTenant();

		var tenantId = currentTenant.getTenant();

		var knnQueryUnis = new ArrayList<Uni<Query>>();

		for (ParserSearchToken parserSearchToken : tokenTypeGroup) {

			var kNeighbors = ParserContext.getInteger(
				parserSearchToken,
				queryParserConfig,
				"kNeighbors"
			).orElse(2);

			for (String value : parserSearchToken.getValues()) {

				var knnQuery = embeddingService
					.getEmbeddedText(tenantId, value)
					.map(embeddedText -> new KnnQuery.Builder()
						.k(kNeighbors)
						.field("vector")
						.vector(toVector(embeddedText))
						.build()
						.toQuery()
					);

				knnQueryUnis.add(knnQuery);

			}

		}

		return Uni.join().all(knnQueryUnis)
			.andCollectFailures()
			.invoke(queries -> {

				for (Query query : queries) {

					try (var os = new ByteArrayOutputStream()) {

						var generator = Json.createGenerator(os);

						query.serialize(generator, new JacksonJsonpMapper());

						var wrapperQueryBuilder = QueryBuilders.wrapperQuery(os.toByteArray());

						parserContext.getMutableQuery().must(wrapperQueryBuilder);

					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

			})
			.replaceWithVoid();
	}

	private static float[] toVector(EmbeddingService.EmbeddedText embeddedText) {

		var list = embeddedText.vector();
		var n = list.size();
		var arr = new float[n];

		for (int i = 0; i < n; i++) {
			arr[i] = list.get(i);
		}

		return arr;
	}

}
