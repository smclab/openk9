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

import io.openk9.model.Datasource;
import io.openk9.search.api.query.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class DatasourceIdQueryParser implements QueryParser {

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {

		return Mono.fromSupplier(() -> {

			List<Datasource> datasources = context.getDatasourceList();

			long[] ids = new long[datasources.size()];

			for (int i = 0; i < datasources.size(); i++) {

				Datasource datasource = datasources.get(i);

				ids[i] = datasource.getDatasourceId();

			}

			return (bool) -> bool.filter(
				QueryBuilders
					.termsQuery("datasourceId", ids)
			);

		});
	}
}
