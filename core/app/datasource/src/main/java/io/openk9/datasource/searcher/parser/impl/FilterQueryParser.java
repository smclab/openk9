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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import io.openk9.datasource.model.QueryParserType;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.QueryType;
import io.openk9.searcher.client.dto.ParserSearchToken;

import io.vertx.core.json.JsonObject;
import org.opensearch.index.query.BoolQueryBuilder;

@ApplicationScoped
@Named("FilterQueryParser")
public class FilterQueryParser extends TextQueryParser implements QueryParser {

	@Override
	public QueryParserType getType() {
		return QueryParserType.FILTER;
	}

	@Override
	protected void doAddTokenClause(
		ParserSearchToken token, JsonObject jsonConfig, BoolQueryBuilder mutableQuery,
		BoolQueryBuilder tokenClauseBuilder) {

		QueryType.FILTER.useConfiguredQueryType(mutableQuery, tokenClauseBuilder);
	}

}
