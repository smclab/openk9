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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import io.openk9.api.tenantmanager.TenantManager;
import io.openk9.auth.tenant.TenantRegistry;
import io.openk9.datasource.service.SchedulerService;
import io.openk9.datasource.service.SchedulerService.DatasourceHealthStatus;
import io.openk9.datasource.service.SchedulerService.HealthStatus;

import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.RequestSpecification;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;

@QuarkusTest
public class SchedulerHealthCheckTest {

	// the health endpoints are served at the server root, not under the
	// application root path, so the RestAssured base path must be cleared.
	private static final String HEALTH_ROOT = "/q/health";
	private static final String SCHEDULER_HEALTH = HEALTH_ROOT + "/group/scheduler";

	@InjectMock
	TenantRegistry tenantRegistry;

	@InjectMock
	SchedulerService schedulerService;

	@CacheName("scheduler-health-tenants")
	Cache tenantCache;

	@BeforeEach
	void clearTenantCache() {
		// the tenant list is cached, so reset it before each scenario
		tenantCache.invalidateAll().await().indefinitely();
	}

	@Test
	void should_return_down_with_data_when_errors_present() {

		// a single tenant with one datasource in ERROR and one RUNNING
		mockTenants("public");
		mockStatuses("public", List.of(
			new DatasourceHealthStatus(1L, "web-crawler", HealthStatus.RUNNING),
			new DatasourceHealthStatus(2L, "file-manager", HealthStatus.ERROR)
		));

		// at least one datasource in error -> DOWN, with the per-tenant detail
		health()
			.get(SCHEDULER_HEALTH)
			.then()
			.statusCode(503)
			.body("status", equalTo("DOWN"))
			.body("checks[0].name", equalTo("scheduler"))
			.body("checks[0].status", equalTo("DOWN"))
			.body("checks[0].data", hasEntry("public.web-crawler", "RUNNING"))
			.body("checks[0].data", hasEntry("public.file-manager", "ERROR"))
			.body("checks[0].data.totalDatasources", equalTo(2))
			.body("checks[0].data.errorCount", equalTo(1));
	}

	@Test
	void should_return_up_when_no_errors() {

		// every datasource is in a healthy state
		mockTenants("public");
		mockStatuses("public", List.of(
			new DatasourceHealthStatus(1L, "web-crawler", HealthStatus.RUNNING),
			new DatasourceHealthStatus(2L, "file-manager", HealthStatus.IDLE)
		));

		// no errors -> UP with errorCount 0
		health()
			.get(SCHEDULER_HEALTH)
			.then()
			.statusCode(200)
			.body("status", equalTo("UP"))
			.body("checks[0].data.totalDatasources", equalTo(2))
			.body("checks[0].data.errorCount", equalTo(0));
	}

	@Test
	void should_return_down_on_infrastructural_failure() {

		// the tenant registry is unreachable
		BDDMockito.given(tenantRegistry.getTenantList())
			.willReturn(Uni.createFrom().failure(
				new RuntimeException("tenant registry unreachable")));

		// an infrastructural failure is always DOWN and carries the error message
		health()
			.get(SCHEDULER_HEALTH)
			.then()
			.statusCode(503)
			.body("status", equalTo("DOWN"))
			.body("checks[0].status", equalTo("DOWN"))
			.body("checks[0].data.error", notNullValue());
	}

	@Test
	void should_not_cache_an_infrastructural_failure() {

		// 1. first probe: the tenant registry is unreachable -> DOWN
		BDDMockito.given(tenantRegistry.getTenantList())
			.willReturn(Uni.createFrom().failure(
				new RuntimeException("tenant registry unreachable")));

		health()
			.get(SCHEDULER_HEALTH)
			.then()
			.statusCode(503)
			.body("status", equalTo("DOWN"));

		// 2. the registry recovers; the failure must not have been cached, so the
		//    very next probe (within the cache TTL) reflects the recovered state
		mockTenants("public");
		mockStatuses("public", List.of(
			new DatasourceHealthStatus(1L, "web-crawler", HealthStatus.RUNNING)
		));

		health()
			.get(SCHEDULER_HEALTH)
			.then()
			.statusCode(200)
			.body("status", equalTo("UP"))
			.body("checks[0].data", hasKey("public.web-crawler"));
	}

	@Test
	void should_not_affect_readiness_liveness_and_aggregate() {

		// a datasource in ERROR makes the scheduler group DOWN...
		mockTenants("public");
		mockStatuses("public", List.of(
			new DatasourceHealthStatus(1L, "web-crawler", HealthStatus.ERROR)
		));

		// ...the group itself is DOWN
		health()
			.get(SCHEDULER_HEALTH)
			.then()
			.statusCode(503)
			.body("status", equalTo("DOWN"));

		// ...but the aggregate /q/health (what gateways/LBs probe) stays UP and
		// does not even include the scheduler group
		health()
			.get(HEALTH_ROOT)
			.then()
			.statusCode(200)
			.body("status", equalTo("UP"))
			.body("checks.name", not(hasItem("scheduler")));

		// ...readiness is unaffected
		health()
			.get(HEALTH_ROOT + "/ready")
			.then()
			.statusCode(200)
			.body("status", equalTo("UP"));

		// ...liveness is unaffected
		health()
			.get(HEALTH_ROOT + "/live")
			.then()
			.statusCode(200)
			.body("status", equalTo("UP"));
	}

	@Test
	void should_not_affect_readiness_and_liveness_on_infrastructural_failure() {

		// the tenant registry is unreachable: the scheduler group is DOWN, but
		// being a dedicated @HealthGroup it must not leak into readiness/liveness
		BDDMockito.given(tenantRegistry.getTenantList())
			.willReturn(Uni.createFrom().failure(
				new RuntimeException("tenant registry unreachable")));

		// readiness is unaffected
		health()
			.get(HEALTH_ROOT + "/ready")
			.then()
			.statusCode(200)
			.body("status", equalTo("UP"));

		// liveness is unaffected
		health()
			.get(HEALTH_ROOT + "/live")
			.then()
			.statusCode(200)
			.body("status", equalTo("UP"));
	}

	@Test
	void should_aggregate_all_tenants() {

		// two registered tenants, each with one datasource
		mockTenants("acme", "other");
		mockStatuses("acme", List.of(
			new DatasourceHealthStatus(1L, "web-crawler", HealthStatus.RUNNING)
		));
		mockStatuses("other", List.of(
			new DatasourceHealthStatus(1L, "web-crawler", HealthStatus.IDLE)
		));

		// the response aggregates both tenants
		health()
			.get(SCHEDULER_HEALTH)
			.then()
			.statusCode(200)
			.body("status", equalTo("UP"))
			.body("checks[0].data", hasKey("acme.web-crawler"))
			.body("checks[0].data", hasKey("other.web-crawler"))
			.body("checks[0].data.totalDatasources", equalTo(2));
	}

	private RequestSpecification health() {
		return given().basePath("/");
	}

	private void mockTenants(String... tenantIds) {
		var tenants = List.of(tenantIds).stream()
			.map(id -> new TenantManager.Tenant(id + ".local", id, id, id, id))
			.toList();

		BDDMockito.given(tenantRegistry.getTenantList())
			.willReturn(Uni.createFrom().item(tenants));
	}

	private void mockStatuses(
		String tenantId, List<DatasourceHealthStatus> statuses) {

		BDDMockito.given(schedulerService.getHealthStatusList(tenantId))
			.willReturn(Uni.createFrom().item(statuses));
	}

}
