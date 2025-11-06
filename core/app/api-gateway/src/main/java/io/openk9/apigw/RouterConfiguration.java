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

package io.openk9.apigw;

import io.openk9.apigw.security.RoutePath;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RouterConfiguration {

	@Autowired(required = false)
	int datasourcePort;
	@Autowired(required = false)
	int searcherPort;

	@Value("${io.openk9.apigw.datasource-url:}")
	String datasourceUrl;
	@Value("${io.openk9.apigw.searcher-url:}")
	String searcherUrl;

	@Bean
	RouteLocator locatorBuilder(RouteLocatorBuilder builder) {

		String datasource = datasourcePort > 0
			? "http://localhost:" + datasourcePort
			: datasourceUrl;

		String searcher = searcherPort > 0
			? "http://localhost:" + searcherPort
			: searcherUrl;

		var routes = builder.routes();

		for (RoutePath routePath : RoutePath.values()) {
			Object ignore = switch (routePath) {
				case DATASOURCE_OAUTH2_SETTINGS -> routes.route(
					RoutePath.DATASOURCE_OAUTH2_SETTINGS.name(), r -> r
						.path(RoutePath.DATASOURCE_OAUTH2_SETTINGS.getAntPattern())
						.uri("forward:/oauth2/settings.js")
				);
				case DATASOURCE_PUBLIC_CONFIGS -> routes.route(
					RoutePath.DATASOURCE_PUBLIC_CONFIGS.name(), r -> r
						.path(RoutePath.DATASOURCE_PUBLIC_CONFIGS.getAntPattern())
						.uri(datasource)
				);
				case DATASOURCE -> routes.route(
					RoutePath.DATASOURCE.name(), r -> r
						.path(RoutePath.DATASOURCE.getAntPattern())
						.uri(datasource)
				);
				case SEARCHER -> routes.route(
					RoutePath.SEARCHER.name(), r -> r
						.path(RoutePath.SEARCHER.getAntPattern())
						.uri(searcher)
				);
				case ANY -> routes.route(
					RoutePath.ANY.name(), r -> r
					.path(RoutePath.ANY.getAntPattern())
					.filters(filter -> filter
						.setStatus(200)
						.setResponseHeader("X-Route", "No-Match"))
					.uri("noop://noop")
				);
				// no default case to prevent accidental omissions at compile-time.
			};
		}

		return routes.build();
	}


}
