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

package io.openk9.search.query.internal.parser;

import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class DateQueryParser implements QueryParser {

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {
		return Mono.fromSupplier(() -> {

			List<SearchToken> searchTokens = context
				.getTokenTypeGroup()
				.getOrDefault(TYPE, List.of());

			return bool -> _rangeQuery(
				searchTokens, context.getPluginDriverDocumentTypeList(), bool);

		});
	}

	private void _rangeQuery(
		List<SearchToken> searchTokens,
		List<PluginDriverDTO> pluginDriverDocumentTypeList,
		BoolQueryBuilder bool) {

		if (searchTokens.isEmpty()) {
			return;
		}

		List<SearchKeywordToken> collect =
			pluginDriverDocumentTypeList
				.stream()
				.map(PluginDriverDTO::getDocumentTypes)
				.flatMap(Collection::stream)
				.map(DocumentTypeDTO::getSearchKeywords)
				.flatMap(Collection::stream)
				.filter(SearchKeywordDTO::isDate)
				.flatMap(searchKeywordDTO -> {

					for (SearchToken searchToken : searchTokens) {
						if (searchToken.getKeywordKey() != null
							&& searchToken.getKeywordKey().equals(searchKeywordDTO.getKeyword())) {
							return Stream.of(
								SearchKeywordToken.of(searchKeywordDTO, searchToken));
						}
					}

					return Stream.empty();

				})
				.collect(Collectors.toList());



		if (!collect.isEmpty()) {

			BoolQueryBuilder internalBool = QueryBuilders.boolQuery();

			boolean flag = false;

			for (SearchKeywordToken searchKeywordToken : collect) {

				SearchToken searchToken = searchKeywordToken.getSearchToken();

				Map<String, Object> extra = searchToken.getExtra();

				if (extra == null) {

					String[] values = searchToken.getValues();

					String gte;
					String lte;

					if (values.length != 0) {

						RangeQueryBuilder rangeQueryBuilder =
							QueryBuilders.rangeQuery(
								searchToken.getKeywordKey());

						if (values.length == 1) {
							gte = values[0];
							rangeQueryBuilder.gte(gte);
						}
						else if (values.length == 2) {
							gte = values[0];
							lte = values[1];
							if (gte == null || gte.isBlank()) {
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

					Object gte = extra.get("gte");
					Object lte = extra.get("lte");
					Object lt = extra.get("lt");
					Object gt = extra.get("gt");
					String format =(String)extra.get("format");
					String timeZone = (String)extra.get("time_zone");
					String relation = (String)extra.get("relation");

					RangeQueryBuilder rangeQueryBuilder =
						QueryBuilders.rangeQuery(
							searchToken.getKeywordKey());

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
					if (relation != null && (relation.equals("INTERSECTS") || relation.equals("CONTAINS") || relation.equals("WITHIN"))) {
						rangeQueryBuilder.relation(relation);
					}

					flag = true;

					internalBool.should(rangeQueryBuilder);

				}

			}

			if (flag) {
				bool.must(internalBool);
			}

		}


	}

	private static final String TYPE = "DATE";

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	private static class SearchKeywordToken {
		private final SearchKeywordDTO searchKeyword;
		private final SearchToken searchToken;
	}

}
