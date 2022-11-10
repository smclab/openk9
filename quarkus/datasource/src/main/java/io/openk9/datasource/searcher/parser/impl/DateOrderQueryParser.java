package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.Utils;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;

import javax.enterprise.context.ApplicationScoped;
import java.util.Iterator;

@ApplicationScoped
public class DateOrderQueryParser implements QueryParser {

	@Override
	public String getType() {
		return "DATE_ORDER";
	}

	@Override
	public boolean isQueryParserGroup() {
		return false;
	}

	@Override
	public void accept(ParserContext parserContext) {

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		Bucket currentTenant = parserContext.getCurrentTenant();

		JsonObject queryParserConfig = parserContext.getQueryParserConfig();

		Iterator<String> iterator =
			Utils.getDocTypeFieldsFrom(currentTenant)
				.filter(DocTypeField::getSearchable)
				.filter(DocTypeField::isDate)
				.map(DocTypeField::getName)
				.distinct()
				.iterator();

		if (iterator.hasNext()) {

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			boolQueryBuilder.boost(getBoost(queryParserConfig));

			while (iterator.hasNext()) {

				String keyword = iterator.next();

				FunctionScoreQueryBuilder.FilterFunctionBuilder
					filterFunctionBuilder =
					new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						ScoreFunctionBuilders.linearDecayFunction(
							keyword, null, getScale(queryParserConfig)));

				boolQueryBuilder.should(
					QueryBuilders.functionScoreQuery(
						QueryBuilders.existsQuery(keyword),
						new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
							filterFunctionBuilder}
					)
				);

			}

			mutableQuery.should(boolQueryBuilder);

		}
	}

	static String getScale(JsonObject queryParserConfig) {
		return queryParserConfig.getString("scale", "3650d");
	}

	static Float getBoost(JsonObject queryParserConfig) {
		return queryParserConfig.getFloat("boost", 0.1F);
	}

}
