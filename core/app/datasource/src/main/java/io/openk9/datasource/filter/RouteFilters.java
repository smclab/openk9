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
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.pipeline.resource.PipelineResource;
import io.openk9.datasource.web.Problems;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.vertx.web.RouteFilter;
import io.smallrye.mutiny.Uni;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

public class RouteFilters {

	private static final Logger log = Logger.getLogger(RouteFilters.class);
	// same regex as TenantNameValidator in tenant-manager
	private static final String TENANT_ID_PATTERN = "^[a-z][a-z0-9]{0,62}$";

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@CacheName("tenant-ids")
	Cache tenantCache;

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

		if (log.isDebugEnabled()) {
			log.debugf("Setting '_tenantId' to %s in routingContext", tenantId);
		}

		// set before the check: opening the session resolves the tenant
		// from the routing context, even when it is passed explicitly.
		rc.put("_tenantId", tenantId);

		tenantExists(tenantId)
			.subscribe()
			.with(exists -> {
					if (!exists) {
						endsWithProblem(rc, Problems.invalidTenantId(tenantId));
						return;
					}

					rc.next();
				},
				rc::fail
			);
	}

	// a tenant exists when its schema holds the datasource tables. Any
	// failure of the query rejects the tenant, and only the tenants that
	// exist stay in the cache.
	private Uni<Boolean> tenantExists(String tenantId) {
		return tenantCache
			.getAsync(tenantId, key -> sessionFactory
				.withSession(key, session -> session
					.createQuery(tenantBindingProbe())
					.setMaxResults(1)
					.getResultList())
				.replaceWith(true)
				.onFailure().invoke(failure -> log.warnf(
					"Rejecting tenant '%s': its schema cannot be queried: %s",
					key, failure.getMessage()))
				.onFailure().recoverWithItem(false))
			.call(exists -> exists
				? Uni.createFrom().voidItem()
				: tenantCache.invalidate(tenantId));
	}

	// selects a constant, not the entity: the check needs the table only, so
	// a change to its columns does not turn into an unknown tenant.
	private CriteriaQuery<Integer> tenantBindingProbe() {
		var criteriaBuilder = sessionFactory.getCriteriaBuilder();
		var query = criteriaBuilder.createQuery(Integer.class);
		query.from(TenantBinding.class);

		return query.select(criteriaBuilder.literal(1));
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
