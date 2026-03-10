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

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.openk9.event.tenant.ApiGroup;
import io.openk9.event.tenant.AuthorizationScheme;

public record Preconfiguration(
	SecurityConfiguration name, List<Config> configs) {

	public static Preconfiguration of(
		SecurityConfiguration name, List<Config> configs) {

		return new Preconfiguration(name, configs);
	}

	public record Config(ApiGroup apiGroup, AuthorizationScheme authScheme) {

		public static Config of(ApiGroup apiGroup, AuthorizationScheme authScheme) {
			return new Config(apiGroup, authScheme);
		}
	}

	public static final Map<SecurityConfiguration, List<Config>> PRECONFIGURATION_MAP =
			Map.of(
				SecurityConfiguration.LEGACY,
				List.of(
					Config.of(ApiGroup.ADMINISTRATION, AuthorizationScheme.OAUTH2),
					Config.of(ApiGroup.PUBLIC, AuthorizationScheme.NO_AUTH),
					Config.of(ApiGroup.SEARCH, AuthorizationScheme.NO_AUTH),
					Config.of(ApiGroup.INGESTION, AuthorizationScheme.NO_AUTH)
				),
				SecurityConfiguration.PROFILED_LEGACY,
				List.of(
					Config.of(ApiGroup.ADMINISTRATION, AuthorizationScheme.OAUTH2),
					Config.of(ApiGroup.PUBLIC, AuthorizationScheme.NO_AUTH),
					Config.of(ApiGroup.SEARCH, AuthorizationScheme.OAUTH2),
					Config.of(ApiGroup.INGESTION, AuthorizationScheme.NO_AUTH)
				),
				SecurityConfiguration.PROFILED,
				List.of(
					Config.of(ApiGroup.ADMINISTRATION, AuthorizationScheme.OAUTH2),
					Config.of(ApiGroup.PUBLIC, AuthorizationScheme.API_KEY),
					Config.of(ApiGroup.SEARCH, AuthorizationScheme.OAUTH2),
					Config.of(ApiGroup.INGESTION, AuthorizationScheme.API_KEY)
				),
				SecurityConfiguration.PUBLIC_USAGE,
				List.of(
					Config.of(ApiGroup.ADMINISTRATION, AuthorizationScheme.OAUTH2),
					Config.of(ApiGroup.PUBLIC, AuthorizationScheme.API_KEY),
					Config.of(ApiGroup.SEARCH, AuthorizationScheme.API_KEY),
					Config.of(ApiGroup.INGESTION, AuthorizationScheme.API_KEY)
				),
				SecurityConfiguration.BASIC_AUTH,
				List.of(
					Config.of(ApiGroup.ADMINISTRATION, AuthorizationScheme.NO_AUTH),
					Config.of(ApiGroup.PUBLIC, AuthorizationScheme.NO_AUTH),
					Config.of(ApiGroup.SEARCH, AuthorizationScheme.NO_AUTH),
					Config.of(ApiGroup.INGESTION, AuthorizationScheme.NO_AUTH)
				)
			);

	public static final Set<Preconfiguration> PRECONFIGURATION_SET = fromMap();

	private static Set<Preconfiguration> fromMap() {

		List<Preconfiguration> result = new LinkedList<>();

		for (Map.Entry<SecurityConfiguration, List<Config>> entry
			: PRECONFIGURATION_MAP.entrySet()) {

			result.add(Preconfiguration.of(entry.getKey(), entry.getValue()));
		}

		return new LinkedHashSet<>(result);
	}

}
