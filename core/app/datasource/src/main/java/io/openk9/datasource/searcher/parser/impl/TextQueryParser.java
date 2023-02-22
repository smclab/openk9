package io.openk9.datasource.searcher.parser.impl;

import io.openk9.common.util.function.Predicates;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
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
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ApplicationScoped
public class TextQueryParser implements QueryParser {

	public String getType() {
		return "TEXT";
	}

	@Override
	public void accept(ParserContext parserContext) {

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		Bucket currentTenant = parserContext.getCurrentTenant();

		Set<Datasource> datasources = currentTenant.getDatasources();

		JsonObject queryParserConfig = parserContext.getQueryParserConfig();

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

			Predicate<DocTypeField> keywordKeyPredicate;

			if (keywordKeyIsPresent) {
				keywordKeyPredicate = docTypeField ->
					docTypeField.getFieldName().equals(keywordKey);
			}
			else {
				keywordKeyPredicate = Predicates.positive();
			}

			Map<String, Float> keywordBoostMap =
				docTypeFieldList
					.stream()
					.filter(keywordKeyPredicate)
					.collect(
						Collectors.toMap(
							DocTypeField::getFieldName,
							DocTypeField::getFloatBoost,
							Math::max,
							HashMap::new
						)
					);

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			boolQueryBuilder.boost(getBoost(queryParserConfig));

			QueryType valuesQueryType = getValuesQueryType(queryParserConfig);

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

					valuesQueryType
						.useConfiguredQueryType(
							boolQueryBuilder, multiMatchQueryBuilder);

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

					valuesQueryType
						.useConfiguredQueryType(
							boolQueryBuilder, multiMatchQueryBuilder);

				}

			}

			getGlobalQueryType(queryParserConfig)
				.useConfiguredQueryType(
					mutableQuery, boolQueryBuilder);

		}

	}

	static float getBoost(JsonObject jsonConfig) {
		return jsonConfig.getFloat("boost", 1.0F);
	}

	static QueryType getValuesQueryType(JsonObject jsonConfig) {
		return QueryType.valueOf(
			jsonConfig.getString("valuesQueryType", "SHOULD"));
	}

	static QueryType getGlobalQueryType(JsonObject jsonConfig) {
		return QueryType.valueOf(
			jsonConfig.getString("globalQueryType", "MUST"));
	}

}
