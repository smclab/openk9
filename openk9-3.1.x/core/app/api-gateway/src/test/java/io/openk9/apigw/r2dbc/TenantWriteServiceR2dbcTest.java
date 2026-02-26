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

import io.openk9.event.tenant.Authorization;
import io.openk9.event.tenant.Route;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import({ TenantWriteServiceR2dbc.class })
@ActiveProfiles({"embedded", "test"})
public class TenantWriteServiceR2dbcTest {

	@Autowired
	DatabaseClient dbClient;

	TenantWriteServiceR2dbc service;

	@BeforeEach
	void setup() {
		service = new TenantWriteServiceR2dbc(dbClient);
	}

	@Test
	@DisplayName("Insert Tenant")
	void insertTenant_ok() {
		StepVerifier.create(
			service.insertTenant("tenant1", "example.com", "https://issuer", "client", "secret")
		).verifyComplete();

		StepVerifier.create(
			dbClient.sql("SELECT COUNT(*) AS cnt FROM tenant WHERE tenant_id = 'tenant1'")
				.map((r, m) -> r.get("cnt", Long.class)).one()
		).expectNext(1L).verifyComplete();
	}

	@Test
	@DisplayName("Insert API Key")
	void insertApiKey_ok() {
		StepVerifier.create(
			service.insertTenant("tenant2", "old.com", "old-uri", null, null)
		).verifyComplete();

		StepVerifier.create(
			service.insertApiKey("tenant2", "hash123", "chk")
		).verifyComplete();
	}

	@Test
	@DisplayName("Insert Route Security")
	void insertRouteSecurity_ok() {
		StepVerifier.create(
			service.insertTenant("tenant3", "old.com", "old-uri", null, null)
		).verifyComplete();

		StepVerifier.create(
			service.insertRouteSecurity(
				"tenant3",
				Route.DATASOURCE,
				Authorization.NO_AUTH
			)
		).verifyComplete();
	}

	@Test
	@DisplayName("Update Tenant")
	void updateTenant_ok() {
		// first insert a tenant
		StepVerifier.create(
			service.insertTenant("tenant4", "old.com", "old-uri", "old-client", null)
		).verifyComplete();

		StepVerifier.create(
			dbClient.sql("SELECT host_name, client_id FROM tenant WHERE tenant_id='tenant4'")
				.map((r,m) -> Tuple.tuple(
					r.get("host_name", String.class),
					r.get("client_id", String.class)
				))
				.one()
		).expectNext(Tuple.tuple("old.com", "old-client")).verifyComplete();

		// update
		StepVerifier.create(
			service.updateTenant("tenant4", "new.com", "new-uri", null, null)
		).verifyComplete();

		StepVerifier.create(
			dbClient.sql("SELECT host_name, client_id FROM tenant WHERE tenant_id='tenant4'")
				.map((r,m) -> Tuple.tuple(
					r.get("host_name", String.class),
					r.get("client_id", String.class)
				))
				.one()
		).expectNext(Tuple.tuple("new.com", null)).verifyComplete();
	}

	@Test
	@DisplayName("Delete Tenant")
	void deleteTenant_ok() {
		StepVerifier.create(
			service.insertTenant("tenant5", "example.com", null, null, null)
		).verifyComplete();

		StepVerifier.create(
			service.deleteTenant("tenant5")
		).verifyComplete();
	}

	@Test
	@DisplayName("Delete Tenant Cascade")
	void deleteTenantCascade_ok() {
		var tenantId = "tenant6";

		StepVerifier.create(
			service.insertTenant(tenantId, "old.com", "old-uri", null, null)
				.then(service.insertRouteSecurity(
					tenantId,
					Route.DATASOURCE,
					Authorization.NO_AUTH
				))
				.then(service.insertApiKey(tenantId, "hash123", "chk"))
				.then(service.deleteTenant(tenantId))
		).verifyComplete();

		StepVerifier.create(
			dbClient.sql("SELECT COUNT(*) FROM api_key where tenant_id=$1")
				.bind(1, tenantId)
				.map((row, rowMetadata) -> (int) row.get(0))
				.first()
		).expectNextMatches(count -> count == 0);

		StepVerifier.create(
			dbClient.sql("SELECT COUNT(*) FROM route_security where tenant_id=$1")
				.bind(1, tenantId)
				.map((row, rowMetadata) -> (int) row.get(0))
				.first()
		).expectNextMatches(count -> count == 0);
	}

	@Test
	@DisplayName("Insert Tenant should be idempotent")
	void should_ignore_insert_tenant_with_same_tenantId() {
		var tenantId = "tenant7";

		StepVerifier.create(
			service.insertTenant(tenantId, "tenant7.local", "issuer.tenant7.local", null, null)
				.then(service.insertTenant(tenantId, "tenant7.local", "issuer.tenant7.local", null, null))
		).verifyComplete();


		StepVerifier.create(
			dbClient.sql("SELECT COUNT(*) FROM tenant where tenant_id=$1")
				.bind(1, tenantId)
				.map((row, rowMetadata) -> (int) row.get(0))
				.first()
		).expectNextMatches(count -> count == 1);

	}

	@Test
	@DisplayName("Insert ApiKey should be idempotent")
	void should_ignore_insert_apiKey_with_same_tenantId_and_hash() {
		var tenantId = "tenant7";
		var hash = "hash123";
		var checksum = "chk";

		StepVerifier.create(
			service.insertApiKey(tenantId, hash, checksum)
				.then(service.insertApiKey(tenantId, hash, checksum))
		).verifyComplete();


		StepVerifier.create(
			dbClient.sql("SELECT COUNT(*) FROM api_key where tenant_id=$1 and api_key_hash=$2")
				.bind(1, tenantId)
				.bind(2, hash)
				.map((row, rowMetadata) -> (int) row.get(0))
				.first()
		).expectNextMatches(count -> count == 1);

	}

	@Test
	@DisplayName("Insert RouteSecurity should be idempotent")
	void should_ignore_insert_routeSecurity_with_same_tenantId_and_route() {
		var tenantId = "tenant7";
		var route = Route.DATASOURCE;

		StepVerifier.create(
			service.insertRouteSecurity(
					tenantId, route, Authorization.NO_AUTH)
				.then(service.insertRouteSecurity(
					tenantId, route, Authorization.NO_AUTH))
		).verifyComplete();


		StepVerifier.create(
			dbClient.sql("SELECT COUNT(*) FROM route_security where tenant_id=$1 and route=$2")
				.bind(1, tenantId)
				.bind(2, route)
				.map((row, rowMetadata) -> (int) row.get(0))
				.first()
		).expectNextMatches(count -> count == 1);

	}
}
