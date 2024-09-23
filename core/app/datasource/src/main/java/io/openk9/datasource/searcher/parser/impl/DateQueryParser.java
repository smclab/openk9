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
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vavr.Function0;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.RangeQueryBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ApplicationScoped
public class DateQueryParser implements QueryParser {

	@Override
	public String getType() {
		return "DATE";
	}

	@Override
	public Uni<Void> apply(ParserContext parserContext) {

		List<ParserSearchToken> searchTokens =
			parserContext.getTokenTypeGroup();

		Bucket currentTenant = parserContext.getCurrentTenant();

		JsonObject queryParserConfig = parserContext.getQueryParserConfig();

		Function0<Boolean> allFieldsWhenKeywordIsEmpty =
			Function0
				.of(
					() -> queryParserConfig.getBoolean(
						"allFieldsWhenKeywordIsEmpty", true))
				.memoized();

		List<Tuple2<DocTypeField, ParserSearchToken>> collect =
			Utils.getDocTypeFieldsFrom(currentTenant)
				.filter(DocTypeField::isSearchableAndDate)
				.flatMap(docTypeField -> {

					for (ParserSearchToken searchToken : searchTokens) {

						if (StringUtils.isBlank(searchToken.getKeywordKey())) {

							if (allFieldsWhenKeywordIsEmpty.get()) {
								return Stream.of(
									Tuple2.of(docTypeField, searchToken));
							}
							else {
								continue;
							}

						}

						if (searchToken.getKeywordKey().equals(docTypeField.getPath())) {
							return Stream.of(Tuple2.of(docTypeField, searchToken));
						}
					}

					return Stream.empty();

				}).toList();

		if (!collect.isEmpty()) {

			BoolQueryBuilder internalBool = QueryBuilders.boolQuery();

			boolean flag = false;

			for (Tuple2<DocTypeField, ParserSearchToken> searchKeywordToken : collect) {

				DocTypeField docTypeField = searchKeywordToken.getItem1();

				ParserSearchToken searchToken = searchKeywordToken.getItem2();

				Map<String, String> extra = searchToken.getExtra();

				if (extra == null) {

					List<String> values = searchToken.getValues();

					String gte;
					String lte;

					if (!values.isEmpty()) {

						RangeQueryBuilder rangeQueryBuilder =
							QueryBuilders.rangeQuery(
								docTypeField.getPath());

						if (values.size() == 1) {
							gte = values.get(0);
							rangeQueryBuilder.gte(gte);
						}
						else if (values.size() == 2) {
							gte = values.get(0);
							lte = values.get(1);
							if (StringUtils.isBlank(gte)) {
								rangeQueryBuilder.lte(lte);
							}
							else {
								rangeQueryBuilder
									.gte(gte)
									.lte(lte);
							}
						}

						flag = true;

						internalBool.should(rangeQueryBuilder);

					}
				}
				else {

					String gte = extra.get("gte");
					String lte = extra.get("lte");
					String lt = extra.get("lt");
					String gt = extra.get("gt");
					String format = extra.get("format");
					String timeZone = extra.get("time_zone");
					String relation = extra.get("relation");

					RangeQueryBuilder rangeQueryBuilder =
						QueryBuilders.rangeQuery(
							docTypeField.getPath());

					if (gte != null) {
						rangeQueryBuilder.gte(gte);
					}
					if (lte != null) {
						rangeQueryBuilder.lte(lte);
					}
					if (gt != null) {
						rangeQueryBuilder.gt(gt);
					}
					if (lt != null) {
						rangeQueryBuilder.lt(lt);
					}
					if (format != null) {
						rangeQueryBuilder.format(format);
					}
					if (timeZone != null) {
						rangeQueryBuilder.timeZone(timeZone);
					}
					if (relation != null && (relation.equals("INTERSECTS") ||
											 relation.equals("CONTAINS") ||
											 relation.equals("WITHIN"))) {
						rangeQueryBuilder.relation(relation);
					}

					flag = true;

					internalBool.should(rangeQueryBuilder);

				}

			}

			if (flag) {
				parserContext.getMutableQuery().must(internalBool);
			}

		}

		return Uni.createFrom().voidItem();
	}

}
