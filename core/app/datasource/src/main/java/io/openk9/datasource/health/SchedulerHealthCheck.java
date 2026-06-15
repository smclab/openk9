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

package io.openk9.datasource.health;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.api.tenantmanager.TenantManager;
import io.openk9.auth.tenant.TenantRegistry;
import io.openk9.datasource.service.SchedulerService;
import io.openk9.datasource.service.SchedulerService.DatasourceHealthStatus;
import io.openk9.datasource.service.SchedulerService.HealthStatus;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CompositeCacheKey;
import io.smallrye.health.api.AsyncHealthCheck;
import io.smallrye.health.api.HealthGroup;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Exposes the health of every datasource scheduling across all tenants as a
 * dedicated SmallRye Health group, served at {@code /q/health/group/scheduler}.
 * <p>
 * The group reports {@code DOWN} when at least one datasource is in
 * {@link HealthStatus#ERROR} (or on an infrastructural failure), and {@code UP}
 * otherwise; the per-tenant detail is always returned in the response data.
 */
@ApplicationScoped
@HealthGroup("scheduler")
public class SchedulerHealthCheck implements AsyncHealthCheck {

	private static final String NAME = "scheduler";
	private static final String TOTAL_DATASOURCES = "totalDatasources";
	private static final String ERROR_COUNT = "errorCount";
	private static final String ERROR = "error";

	@Inject
	TenantRegistry tenantRegistry;

	@Inject
	SchedulerService schedulerService;

	@CacheName("scheduler-health-tenants")
	Cache tenantCache;

	/**
	 * Aggregates the scheduler health of all registered tenants into a single
	 * {@link HealthCheckResponse}. Any failure while collecting the data is
	 * recovered into a {@code DOWN} response carrying the error message.
	 *
	 * @return A {@link Uni} that emits the aggregated health check response.
	 */
	@Override
	public Uni<HealthCheckResponse> call() {
		return cachedTenantIdList()
			.flatMap(this::collectStatuses)
			.map(this::buildResponse)
			.onFailure().recoverWithItem(SchedulerHealthCheck::buildErrorResponse);
	}

	/**
	 * Returns the list of tenant ids, cached for a short TTL (configured via
	 * {@code quarkus.cache.caffeine."scheduler-health-tenants".expire-after-write}).
	 * The set of tenants changes rarely, so this avoids hitting the tenant
	 * registry on every health probe. A failure is not cached.
	 */
	private Uni<List<String>> cachedTenantIdList() {
		return tenantCache.getAsync(
			new CompositeCacheKey(NAME, "tenantIdList"),
			key -> tenantRegistry.getTenantList()
				.map(tenants -> tenants.stream()
					.map(TenantManager.Tenant::schemaName)
					.toList())
		);
	}

	private Uni<List<TenantSchedulerStatuses>> collectStatuses(
		List<String> tenantIdList) {

		if (tenantIdList == null || tenantIdList.isEmpty()) {
			return Uni.createFrom().item(List.of());
		}

		List<Uni<TenantSchedulerStatuses>> statuses = new ArrayList<>();

		for (String tenantId : tenantIdList) {
			Uni<TenantSchedulerStatuses> tenantStatuses = schedulerService
				.getHealthStatusList(tenantId)
				.map(datasourceStatuses -> new TenantSchedulerStatuses(
					tenantId, datasourceStatuses));

			statuses.add(tenantStatuses);
		}

		return Uni.join()
			.all(statuses)
			.usingConcurrencyOf(1)
			.andCollectFailures();
	}

	private HealthCheckResponse buildResponse(
		List<TenantSchedulerStatuses> allTenantStatuses) {

		HealthCheckResponseBuilder builder = HealthCheckResponse.named(NAME);

		long total = 0;
		long errorCount = 0;

		for (TenantSchedulerStatuses tenantStatuses : allTenantStatuses) {
			for (DatasourceHealthStatus datasourceStatus : tenantStatuses.datasourceStatuses()) {
				builder.withData(
					tenantStatuses.tenantId() + "." + datasourceStatus.name(),
					datasourceStatus.status().name()
				);

				total++;

				if (datasourceStatus.status() == HealthStatus.ERROR) {
					errorCount++;
				}
			}
		}

		builder.withData(TOTAL_DATASOURCES, total);
		builder.withData(ERROR_COUNT, errorCount);

		if (errorCount > 0) {
			builder.down();
		}
		else {
			builder.up();
		}

		return builder.build();
	}

	private static HealthCheckResponse buildErrorResponse(Throwable throwable) {
		return HealthCheckResponse.named(NAME)
			.down()
			.withData(ERROR, String.valueOf(throwable.getMessage()))
			.build();
	}

	private record TenantSchedulerStatuses(
		String tenantId,
		List<DatasourceHealthStatus> datasourceStatuses
	) {}

}
