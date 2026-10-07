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

import io.openk9.common.model.dto.Problem;
import io.openk9.common.util.web.InternalHeaders;
import io.openk9.datasource.pipeline.resource.PipelineResource;
import io.openk9.datasource.service.TenantRegistry;
import io.openk9.datasource.web.Problems;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

public class RouteFilters {

	private static final Logger log = Logger.getLogger(RouteFilters.class);
	// same regex as TenantNameValidator in tenant-manager: keep them aligned
	private static final String TENANT_ID_PATTERN = "^[a-z][a-z0-9]{0,62}$";

	@Inject
	TenantRegistry tenantRegistry;

	@ConfigProperty(name = "quarkus.http.root-path")
	String rootPath;

	@RouteFilter(Integer.MAX_VALUE - 100)
	void tenantIdRouteFilter(RoutingContext rc) {
		// the enrichers call back without X-TENANT-ID: the tenant is in the
		// callback token, and the callback does not touch the database.
		if (rc.normalizedPath().startsWith(callbackPath())) {
			rc.next();
			return;
		}

		HttpServerRequest request = rc.request();
		MultiMap headers = request.headers();
		String tenantId = headers.get(InternalHeaders.TENANT_ID);

		if (tenantId == null || tenantId.isBlank()) {
			endsWithProblem(rc, Problems.missingTenantId());
			return;
		}

		if (!tenantId.matches(TENANT_ID_PATTERN)) {
			endsWithProblem(rc, Problems.malformedTenantId(tenantId));
			return;
		}

		tenantRegistry
			.getTenantIdList()
			.subscribe()
			.with(tenantList -> {
					if (!tenantList.contains(tenantId)) {
						endsWithProblem(rc, Problems.invalidTenantId(tenantId));
						return;
					}

					if (log.isDebugEnabled()) {
						log.debugf("Setting '_tenantId' to %s in routingContext", tenantId);
					}

					rc.put("_tenantId", tenantId);
					rc.next();

				},
				rc::fail
			);


	}

	private String callbackPath() {
		return rootPath + PipelineResource.PATH + PipelineResource.CALLBACK_PATH;
	}

	private static void endsWithProblem(RoutingContext rc, Problem problem) {
		rc.response()
			.setStatusCode(problem.getStatus())
			.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
			.end(Json.encode(problem));
	}

}
