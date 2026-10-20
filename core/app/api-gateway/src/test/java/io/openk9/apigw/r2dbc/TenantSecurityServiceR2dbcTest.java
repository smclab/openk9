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

package io.openk9.apigw.r2dbc;

import io.openk9.apigw.security.ApiRoute;
import io.openk9.apigw.security.AuthorizationSchemeToken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@ActiveProfiles({"embedded", "test"})
@ExtendWith(OutputCaptureExtension.class)
public class TenantSecurityServiceR2dbcTest {

	private static final String INSERT_ROUTE_SECURITY_SQL = """
		INSERT INTO route_security (id, tenant_id, route, authorization_scheme)
		VALUES (:id, :tenantId, :route, :authorizationScheme)
		""";

	@Autowired
	DatabaseClient dbClient;

	TenantSecurityServiceR2dbc service;

	@BeforeEach
	void setup() {
		service = new TenantSecurityServiceR2dbc(
			dbClient, new ConcurrentMapCache("test"));
	}

	@Test
	@DisplayName("An unknown route in route_security does not break the tenant")
	void getTenantAggregate_skipsUnknownRoute(CapturedOutput output) {
		// setup tenant
		StepVerifier.create(
			new TenantWriteServiceR2dbc(dbClient).insertTenant(
				"unknown-route", "unknown-route.com", null, null, null)
		).verifyComplete();

		// a known route, with a scheme that differs from its fallback,
		// and a route name that ApiRoute no longer defines
		StepVerifier.create(
			insertRouteSecurity(1L, "unknown-route", "DATASOURCE", "NO_AUTH")
				.then(insertRouteSecurity(
					2L, "unknown-route", "DATASOURCE_OAUTH2_SETTINGS_JS",
					"NO_AUTH"))
		).verifyComplete();

		// the tenant loads with the remaining routes
		StepVerifier.create(service.getTenantAggregate("unknown-route"))
			.assertNext(tenant -> assertThat(
				tenant.routeAuthorizationMap().schemeFor(ApiRoute.DATASOURCE))
				.isEqualTo(AuthorizationSchemeToken.NO_AUTH))
			.verifyComplete();

		// the skipped row is reported
		assertThat(output)
			.contains("unknown-route")
			.contains("DATASOURCE_OAUTH2_SETTINGS_JS");
	}

	private Mono<Void> insertRouteSecurity(
		long id, String tenantId, String route, String authorizationScheme) {

		return dbClient.sql(INSERT_ROUTE_SECURITY_SQL)
			.bind("id", id)
			.bind("tenantId", tenantId)
			.bind("route", route)
			.bind("authorizationScheme", authorizationScheme)
			.then();
	}

}
