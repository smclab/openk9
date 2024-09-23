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
import io.openk9.datasource.util.OpenSearchUtils;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensearch.client.opensearch._types.query_dsl.HybridQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.search.builder.SearchSourceBuilder;

@ApplicationScoped
public class HybridQueryParser {

	@ConfigProperty(
		name = "openk9.datasource.acl.query.extra.params.key", defaultValue = "OPENK9_ACL"
	)
	String extraParamsKey;
	@ConfigProperty(
		name = "openk9.datasource.acl.query.extra.params.enabled", defaultValue = "false"
	)
	boolean extraParamsEnabled;

	@Inject
	EmbeddingService embeddingService;

	public Uni<SearchSourceBuilder> apply(
		ParserContext parserContext, SearchSourceBuilder searchSourceBuilder) {

		var bucket = parserContext.getCurrentTenant();
		var jsonConfig = parserContext.getQueryParserConfig();
		var parserSearchToken = parserContext.getTokenTypeGroup().iterator().next();

		var tenantId = bucket.getTenant();

		var kNeighbors = KnnQueryParser.getKNeighbors(parserSearchToken, jsonConfig);
		var boost = TextQueryParser.getBoost(parserSearchToken, jsonConfig);
		var fuzziness = TextQueryParser.getFuzziness(parserSearchToken, jsonConfig);

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
				.map(hybridQuery -> {
					searchSourceBuilder.query(hybridQuery);

					var aclFilterQuery = AclQueryParser.getAclFilterQuery(
						parserContext, extraParamsKey, extraParamsEnabled);

					searchSourceBuilder.postFilter(aclFilterQuery);

					return searchSourceBuilder;
				});

		}

		return Uni.createFrom().item(searchSourceBuilder);
	}

}
