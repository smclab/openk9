package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.util.Fuzziness;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.QueryType;
import io.openk9.datasource.searcher.util.Utils;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class TextQueryParser implements QueryParser {

	public static final String BOOST = "boost";
	public static final String FUZZINESS = "fuzziness";
	public static final String GLOBAL_QUERY_TYPE = "globalQueryType";
	public static final String VALUES_QUERY_TYPE = "valuesQueryType";

	public String getType() {
		return "TEXT";
	}

	@Override
	public void accept(ParserContext parserContext) {

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		Bucket currentTenant = parserContext.getCurrentTenant();

		Set<Datasource> datasources = currentTenant.getDatasources();

		List<DocTypeField> docTypeFieldList =
			Utils.getDocTypeFieldsFrom(datasources)
				.filter(DocTypeField::isSearchableAndText)
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

			tokenClauseBuilder.boost(getBoost(parserContext));

			org.elasticsearch.common.unit.Fuzziness fuzziness = getFuzziness(parserContext);

			QueryType valuesQueryType = getValuesQueryType(parserContext);

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

					multiMatchQueryBuilder.boost(2.0f);

					multiMatchQueryBuilder.fuzziness(fuzziness);

					valuesQueryType
						.useConfiguredQueryType(
							tokenClauseBuilder, multiMatchQueryBuilder);

				}

			}

			doAddTokenClause(parserContext, mutableQuery, tokenClauseBuilder);

		}

	}

	protected void doAddTokenClause(
		ParserContext context,
		BoolQueryBuilder mutableQuery,
		BoolQueryBuilder tokenClauseBuilder) {

		getGlobalQueryType(context)
			.useConfiguredQueryType(mutableQuery, tokenClauseBuilder);

	}

	private static float getBoost(ParserContext context) {
		return getValue(context, BOOST)
			.map(Float::parseFloat)
			.orElse(1.0F);
	}

	private static QueryType getValuesQueryType(ParserContext context) {
		return getValue(context, VALUES_QUERY_TYPE)
			.map(QueryType::valueOf)
			.orElse(QueryType.SHOULD);
	}

	private static QueryType getGlobalQueryType(ParserContext context) {
		return getValue(context, GLOBAL_QUERY_TYPE)
			.map(QueryType::valueOf)
			.orElse(QueryType.MUST);
	}

	private static org.elasticsearch.common.unit.Fuzziness getFuzziness(ParserContext context) {
		return getValue(context, FUZZINESS)
			.map(Fuzziness::valueOf)
			.orElse(Fuzziness.ZERO)
			.toElasticType();
	}

	private static Optional<String> getValue(ParserContext context, String key) {
		Map<String, List<String>> extra = context.getExtraParams();

		if (extra != null && !extra.isEmpty()) {
			List<String> values = extra.get(key);
			if (values != null && values.iterator().hasNext()) {
				return Optional.ofNullable(values.iterator().next());
			}
		}

		JsonObject jsonConfig = context.getQueryParserConfig();
		return Optional.ofNullable(jsonConfig.getString(key));
	}

}
