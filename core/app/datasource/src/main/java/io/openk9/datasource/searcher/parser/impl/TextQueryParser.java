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

import io.openk9.datasource.mapper.FuzzinessMapper;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.util.Fuzziness;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.QueryType;
import io.openk9.datasource.searcher.util.Utils;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.vertx.core.json.JsonObject;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Named;

@ApplicationScoped
@Named("TextQueryParser")
@Default
public class TextQueryParser implements QueryParser {

	public static final String BOOST = "boost";
	public static final String FUZZINESS = "fuzziness";
	public static final String GLOBAL_QUERY_TYPE = "globalQueryType";
	public static final String VALUES_QUERY_TYPE = "valuesQueryType";
	public static final String MULTI_MATCH_TYPE = "multiMatchType";
	public static final String TIE_BREAKER = "tieBreaker";

	public String getType() {
		return "TEXT";
	}

	@Override
	public void accept(ParserContext parserContext) {

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		Bucket currentTenant = parserContext.getCurrentTenant();

		Set<Datasource> datasources = currentTenant.getDatasources();

		String language = parserContext.getLanguage();

		JsonObject jsonConfig = parserContext.getQueryParserConfig();

		List<DocTypeField> docTypeFieldList =
			Utils.getDocTypeFieldsFrom(datasources)
				.filter(DocTypeField::isSearchableAndText)
				.filter(f -> i18nFilter(f, language))
				.toList();

		if (docTypeFieldList.isEmpty()) {
			return;
		}

		for (ParserSearchToken token : parserContext.getTokenTypeGroup()) {

			List<String> values = token.getValues();

			if (values == null || values.isEmpty()) {
				return;
			}

			String keywordKey = token.getKeywordKey();

			boolean keywordKeyIsPresent =
				keywordKey != null && !keywordKey.isBlank();

			Map<String, Float> keywordBoostMap =
				docTypeFieldList
					.stream()
					.filter(docTypeField ->
						!keywordKeyIsPresent || docTypeField.getPath().equals(keywordKey))
					.collect(
						Collectors.toMap(
							DocTypeField::getPath,
							DocTypeField::getFloatBoost,
							Math::max,
							HashMap::new
						)
					);

			BoolQueryBuilder tokenClauseBuilder = QueryBuilders.boolQuery();

			tokenClauseBuilder.boost(getBoost(token, jsonConfig));

			org.opensearch.common.unit.Fuzziness fuzziness = getFuzziness(token, jsonConfig);

			QueryType valuesQueryType = getValuesQueryType(token, jsonConfig);

			for (String value : values) {

				boolean inQuote = Utils.inQuote(value);

				if (inQuote) {
					value = Utils.removeQuote(value);
				}

				int length = Utils.countWords(value);

				if (!inQuote || length == 1) {

					MultiMatchQueryBuilder multiMatchQueryBuilder =
						new MultiMatchQueryBuilder(value);

					multiMatchQueryBuilder.fields(keywordBoostMap);

					multiMatchQueryBuilder.fuzziness(fuzziness);

					multiMatchQueryBuilder.type(getMultiMatchType(token, jsonConfig));

					multiMatchQueryBuilder.tieBreaker(getTieBreaker(token, jsonConfig));

					valuesQueryType
						.useConfiguredQueryType(
							tokenClauseBuilder, multiMatchQueryBuilder);

				}

				if (length > 1) {

					MultiMatchQueryBuilder multiMatchQueryBuilder =
						new MultiMatchQueryBuilder(value);

					multiMatchQueryBuilder.fields(keywordBoostMap);

					multiMatchQueryBuilder.type(
						MultiMatchQueryBuilder.Type.PHRASE);

					if (!inQuote) {
						multiMatchQueryBuilder.slop(2);
					}

					multiMatchQueryBuilder.boost(10.0f);

					valuesQueryType
						.useConfiguredQueryType(
							tokenClauseBuilder, multiMatchQueryBuilder);

				}

			}

			doAddTokenClause(token, jsonConfig, mutableQuery, tokenClauseBuilder);

		}

	}

	protected void doAddTokenClause(
		ParserSearchToken token,
		JsonObject jsonConfig,
		BoolQueryBuilder mutableQuery,
		BoolQueryBuilder tokenClauseBuilder) {

		getGlobalQueryType(token, jsonConfig)
			.useConfiguredQueryType(mutableQuery, tokenClauseBuilder);

	}

	private static boolean i18nFilter(DocTypeField f, String language) {
		String fieldPath = f.getPath();

		if (!language.equals(Language.NONE)) {
			return isI18nOrNotBase(fieldPath, language);
		}
		else {
			return isBaseOrNotI18n(fieldPath);
		}
	}

	private static boolean isBaseOrNotI18n(String fieldPath) {
		return fieldPath.contains(".base") || !fieldPath.contains(".i18n");
	}

	private static boolean isI18nOrNotBase(String fieldPath, String language) {
		return fieldPath.contains(".i18n." + language)
			   || !fieldPath.contains(".base") && !fieldPath.contains(".i18n");
	}

	private static float getBoost(
		ParserSearchToken token, JsonObject jsonConfig) {

		return ParserContext.getFloat(token, jsonConfig, BOOST)
			.orElse(1.0F);
	}

	private static QueryType getValuesQueryType(
		ParserSearchToken token, JsonObject jsonConfig) {

		return ParserContext.getString(token, jsonConfig, VALUES_QUERY_TYPE)
			.map(QueryType::valueOf)
			.orElse(QueryType.SHOULD);
	}

	private static QueryType getGlobalQueryType(
		ParserSearchToken token, JsonObject jsonConfig) {

		return ParserContext.getString(token, jsonConfig, GLOBAL_QUERY_TYPE)
			.map(QueryType::valueOf)
			.orElse(QueryType.MUST);
	}

	private org.opensearch.common.unit.Fuzziness getFuzziness(
		ParserSearchToken token, JsonObject jsonConfig) {

		return FuzzinessMapper.map(ParserContext
			.getString(token, jsonConfig, FUZZINESS)
			.map(Fuzziness::valueOf)
			.orElse(Fuzziness.ZERO)
		);

	}

	private MultiMatchQueryBuilder.Type getMultiMatchType(
		ParserSearchToken token, JsonObject jsonConfig) {

		return ParserContext.getString(token, jsonConfig, MULTI_MATCH_TYPE)
			.map(MultiMatchQueryBuilder.Type::valueOf)
			.orElse(MultiMatchQueryBuilder.Type.MOST_FIELDS);

	}

	private static float getTieBreaker(
		ParserSearchToken token, JsonObject jsonConfig) {

		return ParserContext.getFloat(token, jsonConfig, TIE_BREAKER)
			.orElse(0.0F);
	}

}
