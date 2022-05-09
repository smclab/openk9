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

import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class DocTypeQueryParser implements QueryParser {

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {
		return Mono.fromSupplier(() -> {

			List<SearchToken> searchTokens = context
				.getTokenTypeGroup()
				.getOrDefault(TYPE, List.of());

			return (bool) -> _docTypeBoolQuery(searchTokens, bool);

		});
	}

	private void _docTypeBoolQuery(
		List<SearchToken> searchTokenList, BoolQueryBuilder bool) {

		if (searchTokenList.isEmpty()) {
			return;
		}

		String[][] typeArray =
			searchTokenList
				.stream()
				.map(SearchToken::getValues)
				.toArray(String[][]::new);

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		for (String[] types : typeArray) {

			BoolQueryBuilder shouldBool = QueryBuilders.boolQuery();

			for (String type : types) {
				shouldBool
					.should(
						QueryBuilders
							.matchQuery("documentTypes", type)
							.operator(Operator.AND)
					);
			}

			boolQueryBuilder.must(shouldBool);

		}

		bool.filter(boolQueryBuilder);

	}

	public static final String TYPE = "DOCTYPE";
}
