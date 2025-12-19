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

package io.openk9.event.tenant;

/**
 * Defines the available authorization schemes
 * that would be verified by the API Gateway.
 * <p>
 * It's used in combination with a {@link RouteGroup} to create
 * different security configurations that could be applied to Tenants.
 *
 * @see RouteGroup
 * @see io.openk9.event.tenant.TenantManagementEvent.TenantCreated
 * @see io.openk9.event.tenant.TenantManagementEvent.TenantUpdated
 */
public enum AuthorizationScheme {
	OAUTH2,
	API_KEY,
	NO_AUTH
}
