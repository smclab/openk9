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

import io.openk9.apigw.security.ApiRoute;

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
	@Autowired(required = false)
	int ragPort;
	@Autowired(required = false)
	int ingestionPort;

	@Value("${io.openk9.apigw.datasource-url:}")
	String datasourceUrl;
	@Value("${io.openk9.apigw.searcher-url:}")
	String searcherUrl;
	@Value("${io.openk9.apigw.rag-url:}")
	String ragUrl;
	@Value("${io.openk9.apigw.ingestion-url:}")
	String ingestionUrl;

	@Bean
	RouteLocator locatorBuilder(RouteLocatorBuilder builder) {

		String datasource = datasourcePort > 0
			? "http://localhost:" + datasourcePort
			: datasourceUrl;

		String searcher = searcherPort > 0
			? "http://localhost:" + searcherPort
			: searcherUrl;

		String rag = ragPort > 0
			? "http://localhost:" + ragPort
			: ragUrl;

		String ingestion = ingestionPort > 0
			? "http://localhost:" + ingestionPort
			: ingestionUrl;

		var routes = builder.routes();

		for (ApiRoute apiRoute : ApiRoute.values()) {
			var enforcer = switch (apiRoute) {
				case DATASOURCE_OAUTH2_SETTINGS -> routes.route(
					ApiRoute.DATASOURCE_OAUTH2_SETTINGS.name(), r -> r
						.path(ApiRoute.DATASOURCE_OAUTH2_SETTINGS.getAntPattern())
						.uri("forward:/oauth2/settings")
				);
				case DATASOURCE_OAUTH2_SETTINGS_JS -> routes.route(
					ApiRoute.DATASOURCE_OAUTH2_SETTINGS_JS.name(), r -> r
						.path(ApiRoute.DATASOURCE_OAUTH2_SETTINGS_JS.getAntPattern())
						.uri("forward:/oauth2/settings.js")
				);
				case DATASOURCE_CURRENT_BUCKET -> routes.route(
					ApiRoute.DATASOURCE_CURRENT_BUCKET.name(), r -> r
						.path(ApiRoute.DATASOURCE_CURRENT_BUCKET.getAntPattern())
						.uri(datasource)
				);
				case DATASOURCE_TEMPLATES -> routes.route(
					ApiRoute.DATASOURCE_TEMPLATES.name(), r -> r
						.path(ApiRoute.DATASOURCE_TEMPLATES.getAntPattern())
						.uri(datasource)
				);
				case DATASOURCE -> routes.route(
					ApiRoute.DATASOURCE.name(), r -> r
						.path(ApiRoute.DATASOURCE.getAntPattern())
						.uri(datasource)
				);
				case SEARCHER -> routes.route(
					ApiRoute.SEARCHER.name(), r -> r
						.path(ApiRoute.SEARCHER.getAntPattern())
						.uri(searcher)
				);
				case RAG -> routes.route(
					ApiRoute.RAG.name(), r -> r
						.path(ApiRoute.RAG.getAntPattern())
						.uri(rag)
				);
				case DATASOURCE_PIPELINE_CALLBACK -> routes.route(
					ApiRoute.DATASOURCE_PIPELINE_CALLBACK.name(), r -> r
						.path(ApiRoute.DATASOURCE_PIPELINE_CALLBACK.getAntPattern())
						.uri(datasource)
				);
				case INGESTION -> routes.route(
					ApiRoute.INGESTION.name(), r -> r
						.path(ApiRoute.INGESTION.getAntPattern())
						.uri(ingestion)
				);
				case ANY -> routes.route(
					ApiRoute.ANY.name(), r -> r
						.path(ApiRoute.ANY.getAntPattern())
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
