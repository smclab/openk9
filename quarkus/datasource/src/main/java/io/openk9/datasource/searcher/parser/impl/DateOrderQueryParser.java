package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

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
	public void configure(JsonObject configuration) {
		scale.set(configuration.getString("scale", "3650d"));
		boost.set(configuration.getFloat("boost",  0.1F));
	}

	@Override
	public JsonObject getConfiguration() {
		return new JsonObject()
			.put("scale", scale.get())
			.put("boost", boost.get());
	}

	@Override
	public void accept(ParserContext parserContext) {

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		Tenant currentTenant = parserContext.getCurrentTenant();

		Iterator<String> iterator =
			currentTenant
				.getDatasources()
				.stream()
				.map(Datasource::getDataIndex)
				.map(DataIndex::getDocTypes)
				.flatMap(Collection::stream)
				.map(DocType::getDocTypeFields)
				.flatMap(Collection::stream)
				.filter(DocTypeField::getSearchable)
				.filter(DocTypeField::isDate)
				.map(DocTypeField::getName)
				.distinct()
				.iterator();

		if (iterator.hasNext()) {

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			boolQueryBuilder.boost(boost.get());

			while (iterator.hasNext()) {

				String keyword = iterator.next();

				FunctionScoreQueryBuilder.FilterFunctionBuilder
					filterFunctionBuilder =
					new FunctionScoreQueryBuilder.FilterFunctionBuilder(
						ScoreFunctionBuilders.linearDecayFunction(
							keyword, null, scale.get()));

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

	private final AtomicReference<String> scale =
		new AtomicReference<>("3650d");

	private final AtomicReference<Float> boost =
		new AtomicReference<>(0.1F);


}
