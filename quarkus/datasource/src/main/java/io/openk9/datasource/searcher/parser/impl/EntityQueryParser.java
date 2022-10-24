package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.QueryType;
import io.openk9.searcher.dto.ParserSearchToken;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
					_multiMatchValues(ENTITIES_ID, ids, boost.get()));
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
			queryCondition.get().useConfiguredQueryType(mutableQuery, outerBoolQueryBuilder);
		}

		if (manageEntityName.get()) {
			List<ParserSearchToken> collect =
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
							.build()
					).toList();
			textQueryParser.accept(
				ParserContext.of(collect, mutableQuery, parserContext.getCurrentTenant())
			);
		}

	}

	@Override
	public void configure(JsonObject configuration) {
		boost.set(configuration.getFloat("boost", 50.0f));
		manageEntityName.set(configuration.getBoolean("manageEntityName", true));
		queryCondition.set(
			QueryType.valueOf(
				configuration.getString("queryCondition", "SHOULD")));
	}

	@Override
	public JsonObject getConfiguration() {
		return new JsonObject()
			.put("boost", boost.get())
			.put("manageEntityName", manageEntityName.get())
			.put("queryCondition", queryCondition.get());
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

	private final AtomicReference<Float> boost = new AtomicReference<>(50.0f);
	private final AtomicReference<Boolean> manageEntityName = new AtomicReference<>(true);
	private final AtomicReference<QueryType> queryCondition = new AtomicReference<>(QueryType.SHOULD);

	public static final String TYPE = "ENTITY";
	public static final String ENTITIES_ID = "entities.id";
	public static final String ENTITIES_ENTITY_TYPE = "entities.entityType";
	public static final String ENTITIES_CONTEXT = "entities.context";

	@Inject
	TextQueryParser textQueryParser;

}
