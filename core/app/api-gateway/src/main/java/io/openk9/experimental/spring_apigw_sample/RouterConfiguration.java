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

package io.openk9.experimental.spring_apigw_sample;

import io.openk9.experimental.spring_apigw_sample.security.RoutePath;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Bean
	RouteLocator locatorBuilder(RouteLocatorBuilder builder) {

		log.info(
			"datasource port: {}, searcher port: {}",
			datasourcePort, searcherPort);

		var routes = builder.routes();

		for (RoutePath routePath : RoutePath.values()) {
			switch (routePath) {
				case SEARCHER -> routes.route(
					RoutePath.SEARCHER.name(), r -> r
					.path(RoutePath.SEARCHER.getAntPattern())
					.uri("http://localhost:" + searcherPort)
				);
				case DATASOURCE -> routes.route(
					RoutePath.DATASOURCE.name(), r -> r
					.path(RoutePath.DATASOURCE.getAntPattern())
					.uri("http://localhost:" + datasourcePort)
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
			}
		}

		return routes.build();
	}


}
