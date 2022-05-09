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

package io.openk9.auth.query.parser;

import io.openk9.search.api.query.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class AuthQueryParser implements QueryParser {

	@interface Config {
		boolean enabled() default true;
	}

	@Activate
	@Modified
	void activate(Config config) {
		_enabled = config.enabled();
	}

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {

		if (!_enabled) {
			return NOTHING.apply(context);
		}

		return _addAclQueryParser(context.getAclQuery());
	}

	private Mono<Consumer<BoolQueryBuilder>> _addAclQueryParser(
		String pluginAclQuery) {

		return Mono
			.fromSupplier(() ->
				boolQueryBuilder -> {

					BoolQueryBuilder innerQuery =
						QueryBuilders
							.boolQuery()
							.minimumShouldMatch(1)
							.should(QueryBuilders.matchQuery("acl.public", true));

					QueryBuilder aclQueryBuilder =
						_createAclQuery(pluginAclQuery);

					if (aclQueryBuilder != null) {
						innerQuery.should(aclQueryBuilder);
					}

					boolQueryBuilder.filter(innerQuery);

			}
		);

	}

	private QueryBuilder _createAclQuery(String pluginAclQuery) {

		if (pluginAclQuery == null || pluginAclQuery.isBlank()) {
			return null;
		}
		else {
			return _deserializePluginAclQuery(pluginAclQuery);
		}

	}

	private QueryBuilder _deserializePluginAclQuery(String pluginAclQuery) {
		return QueryBuilders.wrapperQuery(pluginAclQuery);
	}

	private boolean _enabled;

}
