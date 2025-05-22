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

import java.util.Iterator;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.QueryParserType;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;

import io.smallrye.mutiny.Uni;
import org.opensearch.index.query.QueryBuilders;

@ApplicationScoped
public class DatasourceIdQueryParser implements QueryParser {

	@Override
	public QueryParserType getType() {
		return QueryParserType.DATASOURCE_ID;
	}

	@Override
	public Uni<Void> apply(ParserContext parserContext) {

		Bucket bucket = parserContext.getTenantWithBucket().getBucket();

		Set<Datasource> datasources = bucket.getDatasources();

		Iterator<Datasource> iterator = datasources.iterator();

		long[] ids = new long[datasources.size()];

		for (int i = 0; iterator.hasNext(); i++) {

			Datasource datasource = iterator.next();

			ids[i] = datasource.getId();

		}

		parserContext.getMutableQuery().filter(
			QueryBuilders
				.termsQuery("datasourceId", ids)
		);

		return Uni.createFrom().voidItem();
	}

}