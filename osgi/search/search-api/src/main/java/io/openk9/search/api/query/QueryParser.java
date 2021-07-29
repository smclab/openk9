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

package io.openk9.search.api.query;

import io.openk9.model.Datasource;
import io.openk9.model.Tenant;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface QueryParser
	extends Function<
		QueryParser.Context, Mono<Consumer<BoolQueryBuilder>>> {

	default QueryParser andThen(QueryParser after) {

		Objects.requireNonNull(after, "after is null");

		return (l) -> apply(l).flatMap(c1 -> after.apply(l).map(c1::andThen));

	}

	QueryParser NOTHING = (context) -> Mono.just((ignore) -> {});

	@Data
	@RequiredArgsConstructor(staticName = "of")
	class Context {
		final Tenant tenant;
		final List<Datasource> datasourceList;
		final List<PluginDriverDTO> pluginDriverDocumentTypeList;
		final Map<String, List<SearchToken>> tokenTypeGroup;
		final HttpServerRequest httpRequest;
	}

}
