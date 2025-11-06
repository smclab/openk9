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

package io.openk9.datasource.filter;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.jboss.logging.Logger;

public class RouteFilters {

	@RouteFilter(100)
	void tenantIdRouteFilter(RoutingContext rc) {
		HttpServerRequest request = rc.request();
		MultiMap headers = request.headers();
		String tenantId = headers.get("X-TENANT-ID");

		if (tenantId != null) {
			if (log.isDebugEnabled()) {
				log.debugf("Setting '_tenantId' to %s in routingContext", tenantId);
			}

			rc.put("_tenantId", tenantId);
		}
		else {
			if (log.isDebugEnabled()) {
				log.warn("No 'X-TENANT-ID' identified in request headers.");
			}
		}

	}

	private static final Logger log = Logger.getLogger(RouteFilters.class);

}
