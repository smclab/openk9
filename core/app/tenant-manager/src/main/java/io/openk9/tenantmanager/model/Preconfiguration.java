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
import java.util.Set;

public record Preconfiguration(
	PreconfigurationType name, List<Config> configs) {

	public static Preconfiguration of(
		PreconfigurationType name, List<Config> configs) {

		return new Preconfiguration(name, configs);
	}

	public record Config(Route route, AuthScheme authScheme) {

		public static Config of(Route route, AuthScheme authScheme) {
			return new Config(route, authScheme);
		}
	}

	public static final Set<Preconfiguration> PRECONFIGURATIONS =
			Set.of(
				Preconfiguration.of(
					PreconfigurationType.LEGACY,
					List.of(
						Config.of(Route.DATASOURCE_ADMIN, AuthScheme.OAUTH2),
						Config.of(Route.DATASOURCE_PUBLIC, AuthScheme.OPEN),
						Config.of(Route.SEARCHER, AuthScheme.OPEN),
						Config.of(Route.RAG, AuthScheme.OPEN),
						Config.of(Route.INGESTION, AuthScheme.OPEN)
					)
				),
				Preconfiguration.of(
					PreconfigurationType.PROFILED_LEGACY,
					List.of(
						Config.of(Route.DATASOURCE_ADMIN, AuthScheme.OAUTH2),
						Config.of(Route.DATASOURCE_PUBLIC, AuthScheme.OPEN),
						Config.of(Route.SEARCHER, AuthScheme.OAUTH2),
						Config.of(Route.RAG, AuthScheme.OAUTH2),
						Config.of(Route.INGESTION, AuthScheme.OPEN)
					)
				),
				Preconfiguration.of(
					PreconfigurationType.PROFILED,
					List.of(
						Config.of(Route.DATASOURCE_ADMIN, AuthScheme.OAUTH2),
						Config.of(Route.DATASOURCE_PUBLIC, AuthScheme.API_KEY),
						Config.of(Route.SEARCHER, AuthScheme.OAUTH2),
						Config.of(Route.RAG, AuthScheme.OAUTH2),
						Config.of(Route.INGESTION, AuthScheme.API_KEY)
					)
				),
				Preconfiguration.of(
					PreconfigurationType.PUBLIC_USAGE,
					List.of(
						Config.of(Route.DATASOURCE_ADMIN, AuthScheme.OAUTH2),
						Config.of(Route.DATASOURCE_PUBLIC, AuthScheme.API_KEY),
						Config.of(Route.SEARCHER, AuthScheme.API_KEY),
						Config.of(Route.RAG, AuthScheme.API_KEY),
						Config.of(Route.INGESTION, AuthScheme.API_KEY)
					)
				),
				Preconfiguration.of(
					PreconfigurationType.API_KEY_ONLY,
					List.of(
						Config.of(Route.DATASOURCE_ADMIN, AuthScheme.API_KEY),
						Config.of(Route.DATASOURCE_PUBLIC, AuthScheme.API_KEY),
						Config.of(Route.SEARCHER, AuthScheme.API_KEY),
						Config.of(Route.RAG, AuthScheme.API_KEY),
						Config.of(Route.INGESTION, AuthScheme.API_KEY)
					)
				)
			)
		;
}
