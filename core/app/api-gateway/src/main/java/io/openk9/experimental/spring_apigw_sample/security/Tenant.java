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

package io.openk9.experimental.spring_apigw_sample.security;

/**
 * A representation of a Tenant configuration, needed by the Api Gateway
 * {@link org.springframework.security.web.server.SecurityWebFilterChain} to
 * know how to authenticate the user request, for a specific tenant.
 *
 * @param tenantId the tenantId shared across the services
 * @param hostName the hostname of the client request
 * @param issuerUri the issuer that eventually validate the OAuth2 JWT tokens
 * @param keychain the keychain contains the trusted apiKeys for this tenant
 * @param routeAuthorizationMap a map that defines how a route has to be authorized
 */
public record Tenant(
	String tenantId,
	String hostName,
	String issuerUri,
	Keychain keychain,
	RouteAuthorizationMap routeAuthorizationMap) {
}

