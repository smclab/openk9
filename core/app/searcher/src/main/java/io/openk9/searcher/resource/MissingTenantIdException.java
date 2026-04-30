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

package io.openk9.searcher.resource;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Thrown by the searcher resource layer when a request reaches a handler without
 * a tenant identifier in the routing context — typically because the request
 * bypassed the API Gateway (which sets the {@code X-TENANT-ID} header) or the
 * gateway's tenant resolution failed upstream. Maps to HTTP 400.
 */
public class MissingTenantIdException extends WebApplicationException {

	public MissingTenantIdException() {
		super(
			"Missing tenantId: ensure the request carries the X-TENANT-ID header set by the API Gateway",
			Response.Status.BAD_REQUEST
		);
	}
}
