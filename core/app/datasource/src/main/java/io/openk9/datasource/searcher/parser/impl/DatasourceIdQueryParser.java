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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.openk9.datasource.model.util.K9Entity;
import io.openk9.searcher.client.dto.ParserSearchToken;
import jakarta.enterprise.context.ApplicationScoped;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.QueryParserType;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;

import io.smallrye.mutiny.Uni;
import org.opensearch.index.query.QueryBuilders;

@ApplicationScoped
public class DatasourceIdQueryParser implements QueryParser {

	@Override
	public QueryParserType getType() {
		return QueryParserType.DATASOURCE;
	}

	@Override
	public Uni<Void> apply(ParserContext parserContext) {

		Bucket bucket = parserContext.getTenantWithBucket().getBucket();

		Set<Long> datasourceIds = bucket.getDatasources().stream()
			.map(K9Entity::getId)
			.collect(Collectors.toSet());

		List<ParserSearchToken> tokens = parserContext.getTokenTypeGroup();

		List<Long> ids = tokens.stream()
			.map(ParserSearchToken::getValues)
			.flatMap(Collection::stream)
			.map(Long::parseLong)
			.filter(datasourceIds::contains)
			.toList();

		parserContext.getMutableQuery().filter(
			QueryBuilders
				.termsQuery("datasourceId", ids)
		);

		return Uni.createFrom().voidItem();
	}

}