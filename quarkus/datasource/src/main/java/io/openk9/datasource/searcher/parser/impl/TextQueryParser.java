package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.QueryType;
import io.openk9.datasource.searcher.util.Utils;
import io.openk9.searcher.dto.ParserSearchToken;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
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

		Tenant currentTenant = parserContext.getCurrentTenant();

		Set<Datasource> datasources = currentTenant.getDatasources();

		List<DocTypeField> docTypeFieldList =
			datasources
				.stream()
				.map(Datasource::getDataIndex)
				.map(DataIndex::getDocTypes)
				.flatMap(Collection::stream)
				.map(DocType::getDocTypeFields)
				.flatMap(Collection::stream)
				.filter(DocTypeField::isText)
				.filter(DocTypeField::getSearchable)
				.toList();

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
					docTypeField.getName().equals(keywordKey);
			}
			else {
				keywordKeyPredicate = __ -> true;
			}

			Map<String, Float> keywordBoostMap =
				docTypeFieldList
					.stream()
					.filter(keywordKeyPredicate)
					.collect(
						Collectors.toMap(
							DocTypeField::getName,
							DocTypeField::getFloatBoost,
							Math::max,
							HashMap::new
						)
					);

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			boolQueryBuilder.boost(boost.get());

			for (String value : values) {

				boolean inQuote = Utils.inQuote(value);

				if (inQuote) {
					value = Utils.removeQuote(value);
				}

				int length = value.split("\\s+").length;

				if (!inQuote || length == 1) {

					MultiMatchQueryBuilder multiMatchQueryBuilder =
						new MultiMatchQueryBuilder(value);

					multiMatchQueryBuilder.fields(keywordBoostMap);

					boolQueryBuilder.should(multiMatchQueryBuilder);

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

					valuesQueryType.get().useConfiguredQueryType(
						boolQueryBuilder, multiMatchQueryBuilder);

				}

			}

			globalQueryType.get().useConfiguredQueryType(
				mutableQuery, boolQueryBuilder);

		}

	}

	@Override
	public void configure(JsonObject configuration) {
		boost.set(configuration.getFloat("boost", 1.0f));
		valuesQueryType.set(
			QueryType.valueOf(
				configuration.getString("valuesQueryType", QueryType.SHOULD.name())));
		globalQueryType.set(
			QueryType.valueOf(
				configuration.getString("globalQueryType", QueryType.MUST.name())));
	}

	@Override
	public JsonObject getConfiguration() {
		return new JsonObject()
			.put("boost", boost.get())
			.put("valuesQueryType", valuesQueryType.get().name())
			.put("globalQueryType", globalQueryType.get().name());
	}

	private final AtomicReference<Float> boost =
		new AtomicReference<>(1.0F);

	private final AtomicReference<QueryType> valuesQueryType =
		new AtomicReference<>(QueryType.SHOULD);

	private final AtomicReference<QueryType> globalQueryType =
		new AtomicReference<>(QueryType.MUST);

}
