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

import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.searcher.client.dto.ParserSearchToken;
import io.smallrye.mutiny.Uni;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.Operator;
import org.opensearch.index.query.QueryBuilders;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DocTypeQueryParser implements QueryParser {

	@Override
	public String getType() {
		return "DOCTYPE";
	}

	@Override
	public Uni<Void> apply(ParserContext parserContext) {

		List<ParserSearchToken> tokenTypeGroup =
			parserContext.getTokenTypeGroup();

		if (tokenTypeGroup.isEmpty()) {
			return Uni.createFrom().voidItem();
		}

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		List<List<String>> typeList =
			tokenTypeGroup
				.stream()
				.map(ParserSearchToken::getValues)
				.toList();

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		for (List<String> types : typeList) {
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

		mutableQuery.filter(boolQueryBuilder);

		return Uni.createFrom().voidItem();
	}


}
