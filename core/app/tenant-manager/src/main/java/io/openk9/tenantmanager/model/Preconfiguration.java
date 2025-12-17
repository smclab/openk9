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

package io.openk9.tenantmanager.model;

import java.util.List;

public record Preconfiguration(
	PreconfigurationType name, List<Config> configs) {

	public record Config(Route route, AuthScheme authScheme) {}

	public static final List<Preconfiguration> PRECONFIGURATIONS =
			List.of(
				new Preconfiguration(
					PreconfigurationType.LEGACY,
					List.of(
						new Config(Route.DATASOURCE_ADMIN, AuthScheme.OAUTH2),
						new Config(Route.DATASOURCE_PUBLIC, AuthScheme.OPEN),
						new Config(Route.SEARCHER, AuthScheme.OPEN),
						new Config(Route.RAG, AuthScheme.OPEN),
						new Config(Route.INGESTION, AuthScheme.OPEN)
					)
				),
				new Preconfiguration(
					PreconfigurationType.PROFILED_LEGACY,
					List.of(
						new Config(Route.DATASOURCE_ADMIN, AuthScheme.OAUTH2),
						new Config(Route.DATASOURCE_PUBLIC, AuthScheme.OPEN),
						new Config(Route.SEARCHER, AuthScheme.OAUTH2),
						new Config(Route.RAG, AuthScheme.OAUTH2),
						new Config(Route.INGESTION, AuthScheme.OPEN)
					)
				),
				new Preconfiguration(
					PreconfigurationType.PROFILED,
					List.of(
						new Config(Route.DATASOURCE_ADMIN, AuthScheme.OAUTH2),
						new Config(Route.DATASOURCE_PUBLIC, AuthScheme.API_KEY),
						new Config(Route.SEARCHER, AuthScheme.OAUTH2),
						new Config(Route.RAG, AuthScheme.OAUTH2),
						new Config(Route.INGESTION, AuthScheme.API_KEY)
					)
				),
				new Preconfiguration(
					PreconfigurationType.PUBLIC_USAGE,
					List.of(
						new Config(Route.DATASOURCE_ADMIN, AuthScheme.OAUTH2),
						new Config(Route.DATASOURCE_PUBLIC, AuthScheme.API_KEY),
						new Config(Route.SEARCHER, AuthScheme.API_KEY),
						new Config(Route.RAG, AuthScheme.API_KEY),
						new Config(Route.INGESTION, AuthScheme.API_KEY)
					)
				),
				new Preconfiguration(
					PreconfigurationType.API_KEY_ONLY,
					List.of(
						new Config(Route.DATASOURCE_ADMIN, AuthScheme.API_KEY),
						new Config(Route.DATASOURCE_PUBLIC, AuthScheme.API_KEY),
						new Config(Route.SEARCHER, AuthScheme.API_KEY),
						new Config(Route.RAG, AuthScheme.API_KEY),
						new Config(Route.INGESTION, AuthScheme.API_KEY)
					)
				)
			)
		;
}
