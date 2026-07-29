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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import io.openk9.datasource.index.util.OpenSearchUtils;
import io.openk9.datasource.model.QueryParserType;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;

import io.smallrye.mutiny.Uni;
import org.opensearch.client.opensearch._types.query_dsl.HybridQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.search.builder.SearchSourceBuilder;

@ApplicationScoped
@Named("HybridQueryParser")
public class HybridQueryParser implements QueryParser {

	@Inject
	AclQueryParser aclQueryParser;

	@Inject
	EmbeddingService embeddingService;

	@Override
	public Uni<Void> apply(ParserContext parserContext) {
		throw new UnsupportedOperationException(
			"Hybrid query parser cannot be applied to standard queries.");
	}

	@Override
	public QueryParserType getType() {
		return QueryParserType.HYBRID;
	}

	public Uni<SearchSourceBuilder> apply(
		ParserContext parserContext, SearchSourceBuilder searchSourceBuilder) {

		var jsonConfig = parserContext.getQueryParserConfig();
		var parserSearchToken = parserContext.getTokenTypeGroup().iterator().next();

		var tenantId = parserContext.getTenantWithBucket().getTenantId();

		var kNeighbors = KnnQueryParser.getKNeighbors(parserSearchToken, jsonConfig);
		var boost = TextQueryParser.getBoost(parserSearchToken, jsonConfig);
		var fuzziness = TextQueryParser.getFuzziness(parserSearchToken, jsonConfig);

		var values = parserSearchToken.getValues().iterator();

		var searchConfig = parserContext.getTenantWithBucket().getBucket().getSearchConfig();

		var maxTextQueryLength =
			(searchConfig != null) && (searchConfig.getMaxTextQueryLength() != null)
			? searchConfig.getMaxTextQueryLength()
			: DEFAULT_MAX_TEXT_QUERY_LENGTH;

		if (values.hasNext()) {
			var value = values.next();

			// enforce a maximum text query length (disabled if set to 0 or a negative value)
			var textQueryValue = (maxTextQueryLength > 0 && value.length() > maxTextQueryLength)
				? value.substring(0, maxTextQueryLength)
				: value;

			var matchQuery = new MatchQuery.Builder()
				.field("chunkText")
				.query(q -> q.stringValue(textQueryValue))
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
				.map(hybridQuery -> {
					searchSourceBuilder.query(hybridQuery);

					var aclFilterQuery = 
						aclQueryParser.getBoolQuery(
							parserContext);

					searchSourceBuilder.postFilter(
						aclFilterQuery);

					return searchSourceBuilder;
				});

		}

		return Uni.createFrom().item(searchSourceBuilder);
	}

	protected AclQueryParser getAclQueryParser() {
		return this.aclQueryParser;
	}

}
