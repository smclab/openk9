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

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Defines the gateway-level authorization model for a tenant.
 * Each value maps to a {@link Preconfiguration} that assigns
 * an {@link io.openk9.event.tenant.AuthorizationScheme} per
 * {@link io.openk9.event.tenant.ApiGroup}.
 */
public enum SecurityConfiguration {

	/** Admin routes require OAuth2. Public, search, and
	 *  ingestion are open (NO_AUTH). */
	@Schema(description = Desc.OAUTH2_ADMIN_ONLY)
	@Description(Desc.OAUTH2_ADMIN_ONLY)
	OAUTH2_ADMIN_ONLY,

	/** Admin and search require OAuth2. Public and
	 *  ingestion are open (NO_AUTH). */
	@Schema(description = Desc.OAUTH2_SEARCH)
	@Description(Desc.OAUTH2_SEARCH)
	OAUTH2_SEARCH,

	/** Admin and search require OAuth2. Public and
	 *  ingestion require API keys. */
	@Schema(description = Desc.OAUTH2_SEARCH_WITH_API_KEY)
	@Description(Desc.OAUTH2_SEARCH_WITH_API_KEY)
	OAUTH2_SEARCH_WITH_API_KEY,

	/** Admin requires OAuth2. Public, search, and
	 *  ingestion require API keys. */
	@Schema(description = Desc.OAUTH2_ADMIN_WITH_API_KEY)
	@Description(Desc.OAUTH2_ADMIN_WITH_API_KEY)
	OAUTH2_ADMIN_WITH_API_KEY,

	/** No gateway-level authorization on any route. */
	@Schema(description = Desc.NO_GATEWAY_AUTH)
	@Description(Desc.NO_GATEWAY_AUTH)
	NO_GATEWAY_AUTH;

	/**
	 * Whether this security configuration requires an OAuth2
	 * identity provider (Keycloak or external). Returns
	 * {@code true} for all configurations except
	 * {@code NO_GATEWAY_AUTH}, which bypasses gateway
	 * authentication entirely.
	 *
	 * @return true if OAuth2 is needed
	 */
	public boolean requiresOAuth2() {
		return this != NO_GATEWAY_AUTH;
	}

	/** Constant values shared by {@code @Schema} and
	 *  {@code @Description} annotations. */
	interface Desc {

		String OAUTH2_ADMIN_ONLY =
			"OAuth2 for admin only, "
			+ "search and data access are open";

		String OAUTH2_SEARCH =
			"OAuth2 for admin and search, "
			+ "data access is open";

		String OAUTH2_SEARCH_WITH_API_KEY =
			"OAuth2 for admin and search, "
			+ "API keys for data and ingestion";

		String OAUTH2_ADMIN_WITH_API_KEY =
			"OAuth2 for admin only, "
			+ "API keys for all other routes";

		String NO_GATEWAY_AUTH =
			"No gateway auth, "
			+ "downstream services handle security";
	}

}
