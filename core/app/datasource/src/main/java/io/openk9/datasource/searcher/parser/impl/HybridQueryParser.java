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
import io.openk9.datasource.model.SearchConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import io.openk9.datasource.index.util.OpenSearchUtils;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.QueryParserType;
import io.openk9.datasource.pipeline.service.EmbeddingService;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.Utils;
import io.openk9.searcher.client.dto.ParserSearchToken;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensearch.client.opensearch._types.query_dsl.HybridQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.MultiMatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.common.unit.Fuzziness;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Named("HybridQueryParser")
public class HybridQueryParser implements QueryParser {

	static final String CHUNK_TEXT = "chunkText";

	@Inject
	AclQueryParser aclQueryParser;

	@Inject
	EmbeddingService embeddingService;

	/**
	 * This value is only used if the associated {@link SearchConfig} entity
	 * with {@link Bucket} does not have a configured value (is {@code null}). Otherwise, the value
	 * from SearchConfig takes priority.
	 *
	 * @deprecated Configure the value directly in the {@link SearchConfig} entity.
	 *             This property is maintained only as a fallback.
	 */
	@Deprecated
	@ConfigProperty(
		// use 0 or a negative value to disable maximum text query length enforcement
		name = "openk9.datasource.query-parser.max-text-query-length",
		defaultValue = "0"
	)
	Integer defaultMaxTextQueryLength;

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
			: defaultMaxTextQueryLength;

		if (values.hasNext()) {
			var value = values.next();

			// enforce a maximum text query length (disabled if set to 0 or a negative value)
			var textQueryValue = (maxTextQueryLength > 0 && value.length() > maxTextQueryLength)
				? value.substring(0, maxTextQueryLength)
				: value;

			var textQuery = toTextQuery(
				parserContext,
				parserSearchToken,
				jsonConfig,
				textQueryValue,
				fuzziness,
				boost
			);

			return embeddingService.getEmbeddedText(tenantId, value)
				.map(embeddedText -> KnnQueryParser.toKnnQuery(embeddedText, kNeighbors))
				.map(knnQuery -> new HybridQuery.Builder()
					.queries(textQuery, knnQuery)
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

	/**
	 * Builds the textual branch of the hybrid query as a multi match over the
	 * searchable text fields of the bucket, each with its configured boost,
	 * plus the {@code chunkText} field holding the embedded chunk.
	 * <p>
	 * When the bucket exposes no searchable text field, it falls back to a
	 * plain match on {@code chunkText}.
	 *
	 * @param textQueryValue the search text, already truncated if needed
	 * @return the textual query to combine with the knn one
	 */
	static Query toTextQuery(
		ParserContext parserContext,
		ParserSearchToken parserSearchToken,
		JsonObject jsonConfig,
		String textQueryValue,
		Fuzziness fuzziness,
		float boost) {

		var bucket = parserContext.getTenantWithBucket().getBucket();
		var language = parserContext.getLanguage();

		var searchableFields = Utils.getDocTypeFieldsFrom(bucket.getDatasources())
			.filter(DocTypeField::isSearchableAndText)
			.filter(docTypeField -> TextQueryParser.i18nFilter(docTypeField, language))
			.map(HybridQueryParser::toFieldWithBoost)
			.distinct()
			.toList();

		if (searchableFields.isEmpty()) {
			return new MatchQuery.Builder()
				.field(CHUNK_TEXT)
				.query(q -> q.stringValue(textQueryValue))
				.fuzziness(fuzziness.asString())
				.boost(boost)
				.build()
				.toQuery();
		}

		var fields = new ArrayList<String>();
		fields.add(CHUNK_TEXT);
		fields.addAll(searchableFields);

		var multiMatchType = TextQueryParser.getMultiMatchType(
			parserSearchToken, jsonConfig);

		var tieBreaker = TextQueryParser.getTieBreaker(
			parserSearchToken, jsonConfig);

		return new MultiMatchQuery.Builder()
			.fields(fields)
			.query(textQueryValue)
			.type(toTextQueryType(multiMatchType))
			.tieBreaker((double) tieBreaker)
			.fuzziness(fuzziness.asString())
			.boost(boost)
			.build()
			.toQuery();
	}

	private static String toFieldWithBoost(DocTypeField docTypeField) {
		var path = docTypeField.getPath();

		return docTypeField.isDefaultBoost()
			? path + "^" + docTypeField.getFloatBoost()
			: path;
	}

	private static TextQueryType toTextQueryType(
		MultiMatchQueryBuilder.Type type) {

		return switch (type) {
			case BEST_FIELDS -> TextQueryType.BestFields;
			case MOST_FIELDS -> TextQueryType.MostFields;
			case CROSS_FIELDS -> TextQueryType.CrossFields;
			case PHRASE -> TextQueryType.Phrase;
			case PHRASE_PREFIX -> TextQueryType.PhrasePrefix;
			case BOOL_PREFIX -> TextQueryType.BoolPrefix;
		};
	}

	protected AclQueryParser getAclQueryParser() {
		return this.aclQueryParser;
	}

}
