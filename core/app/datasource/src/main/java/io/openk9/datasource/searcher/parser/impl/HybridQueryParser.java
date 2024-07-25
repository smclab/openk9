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
import io.openk9.datasource.util.OpenSearchUtils;
import io.smallrye.mutiny.Uni;
import org.opensearch.client.opensearch._types.query_dsl.HybridQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HybridQueryParser implements QueryParser {

	@Inject
	EmbeddingService embeddingService;

	@Override
	public String getType() {
		return "HYBRID";
	}

	@Override
	public Uni<Void> apply(ParserContext parserContext) {

		var currentTenant = parserContext.getCurrentTenant();

		var tenantId = currentTenant.getTenant();

		var mutableQuery = parserContext.getMutableQuery();

		var queryParserConfig = parserContext.getQueryParserConfig();

		var parserSearchTokens = parserContext.getTokenTypeGroup().iterator();

		if (parserSearchTokens.hasNext()) {
			var parserSearchToken = parserSearchTokens.next();

			var kNeighbors = KnnQueryParser.getKNeighbors(parserSearchToken, queryParserConfig);
			var boost = TextQueryParser.getBoost(parserSearchToken, queryParserConfig);
			var fuzziness = TextQueryParser.getFuzziness(parserSearchToken, queryParserConfig);

			var values = parserSearchToken.getValues().iterator();

			if (values.hasNext()) {
				var value = values.next();

				var matchQuery = new MatchQuery.Builder()
					.field("chunkText")
					.query(q -> q.stringValue(value))
					.fuzziness(fuzziness.asString())
					.boost(boost)
					.build()
					.toQuery();

				return embeddingService.getEmbeddedText(tenantId, value)
					.map(embeddedText -> KnnQueryParser.toKnnQuery(embeddedText, kNeighbors))
					.map(knnQuery -> new HybridQuery.Builder()
						.queries(matchQuery, knnQuery)
						.build()
						.toQuery()
					)
					.map(OpenSearchUtils::toWrapperQueryBuilder)
					.invoke(hybridQuery -> mutableQuery.must(hybridQuery))
					.replaceWithVoid();

			}

		}

		return Uni.createFrom().voidItem();
	}

}
