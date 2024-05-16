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
import io.openk9.datasource.searcher.SearcherService;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.QueryType;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@ApplicationScoped
public class EntityQueryParser implements QueryParser {

	@Override
	public String getType() {
		return "ENTITY";
	}

	@Override
	public void accept(ParserContext parserContext) {

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		BoolQueryBuilder outerBoolQueryBuilder =
			QueryBuilders.boolQuery();

		JsonObject queryParserConfig = parserContext.getQueryParserConfig();

		String language = parserContext.getLanguage();

		Map<String, List<ParserSearchToken>> searchTokenGroupingByType =
			parserContext
				.getTokenTypeGroup()
				.stream()
				.collect(
					Collectors.groupingBy(
						searchToken ->
							StringUtils.isNotBlank(searchToken.getEntityType())
								? ""
								: searchToken.getEntityType()
					)
				);

		boolean addOuterBoolQuery = false;

		for (Map.Entry<String, List<ParserSearchToken>> groupSearchTokens : searchTokenGroupingByType.entrySet()) {

			String type = groupSearchTokens.getKey();

			if (!type.isBlank()) {
				outerBoolQueryBuilder
					.must(
						QueryBuilders.matchQuery(
							ENTITIES_ENTITY_TYPE, type));
				addOuterBoolQuery = true;
			}

			List<ParserSearchToken> value = groupSearchTokens.getValue();

			String[] ids =
				value
					.stream()
					.map(ParserSearchToken::getValues)
					.flatMap(Collection::stream)
					.distinct()
					.toArray(String[]::new);

			if (ids.length != 0) {
				outerBoolQueryBuilder.must(
					_multiMatchValues(ENTITIES_ID, ids, getBoost(queryParserConfig)));
				addOuterBoolQuery = true;
			}

			String[] keywordKeys =
				value
					.stream()
					.map(ParserSearchToken::getKeywordKey)
					.filter(StringUtils::isNotBlank)
					.toArray(String[]::new);

			if (keywordKeys.length != 0) {
				outerBoolQueryBuilder.must(
					_multiMatchValues(
						ENTITIES_CONTEXT, keywordKeys, 1.0f));
				addOuterBoolQuery = true;
			}

		}

		if (addOuterBoolQuery) {
			getQueryCondition(queryParserConfig)
				.useConfiguredQueryType(mutableQuery, outerBoolQueryBuilder);
		}

		if (getManageEntityName(queryParserConfig)) {

			List<ParserSearchToken> textParserSearchTokenList =
				parserContext.getTokenTypeGroup()
					.stream()
					.map(ParserSearchToken::getEntityName)
					.filter(StringUtils::isNotBlank)
					.distinct()
					.map(entityName ->
						ParserSearchToken
							.builder()
							.values(List.of(entityName))
							.tokenType(textQueryParser.getType())
							.build())
					.toList();

			Bucket currentTenant = parserContext.getCurrentTenant();

			JsonObject textQueryParserConfig =
				SearcherService.getQueryParserConfig(
					currentTenant, textQueryParser.getType());

			textQueryParser.accept(
				ParserContext
					.builder()
					.mutableQuery(mutableQuery)
					.currentTenant(parserContext.getCurrentTenant())
					.tokenTypeGroup(textParserSearchTokenList)
					.queryParserConfig(textQueryParserConfig)
					.language(language)
					.build()
			);
		}

	}

	static Float getBoost(JsonObject queryParserConfig) {
		return queryParserConfig.getFloat("boost", 50.0f);
	}

	static Boolean getManageEntityName(JsonObject queryParserConfig) {
		return queryParserConfig.getBoolean("manageEntityName", true);
	}

	static QueryType getQueryCondition(JsonObject queryParserConfig) {
		return QueryType.valueOf(
			queryParserConfig.getString("queryCondition", "SHOULD"));
	}

	private QueryBuilder _multiMatchValues(
		String field, String[] ids, float boost) {

		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		for (String id : ids) {
			boolQuery.should(QueryBuilders.matchQuery(field, id));
		}

		boolQuery.boost(boost);

		return boolQuery;

	}

	public static final String TYPE = "ENTITY";
	public static final String ENTITIES_ID = "entities.id";
	public static final String ENTITIES_ENTITY_TYPE = "entities.entityType";
	public static final String ENTITIES_CONTEXT = "entities.context";

	@Inject
	@Named("TextQueryParser")
	TextQueryParser textQueryParser;

}
