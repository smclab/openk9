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
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.Utils;
import io.vertx.core.json.JsonObject;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.opensearch.index.query.functionscore.ScoreFunctionBuilders;

import java.util.Iterator;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DateOrderQueryParser implements QueryParser {

	@Override
	public String getType() {
		return "DATE_ORDER";
	}

	@Override
	public boolean isQueryParserGroup() {
		return true;
	}

	@Override
	public void accept(ParserContext parserContext) {

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		Bucket currentTenant = parserContext.getCurrentTenant();

		JsonObject queryParserConfig = parserContext.getQueryParserConfig();

		Iterator<String> iterator =
			Utils.getDocTypeFieldsFrom(currentTenant)
				.filter(DocTypeField::isSearchableAndDate)
				.map(DocTypeField::getPath)
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
