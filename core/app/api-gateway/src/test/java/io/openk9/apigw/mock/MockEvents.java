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

package io.openk9.apigw.mock;

import java.util.List;
import java.util.Map;

import io.openk9.event.tenant.Authorization;
import io.openk9.event.tenant.Route;
import io.openk9.event.tenant.TenantManagementEvent;

public class MockEvents {

	public static List<TenantManagementEvent.TenantCreated> tenantCreatedEvents() {
		return List.of(

			TenantManagementEvent.TenantCreated.builder()
				.tenantId("loguetown")
				.schemaName("loguetown")
				.hostName("loguetown.localhost")
				.routeAuthorizationMap(Map.of(
					Route.DATASOURCE, Authorization.API_KEY
				))
				.build(),

			TenantManagementEvent.TenantCreated.builder()
				.tenantId("drum")
				.schemaName("drum")
				.hostName("drum.localhost")
				.issuerUri("http://drum.localhost:9090/realms/drum")
				.clientId("openk9")
				.routeAuthorizationMap(Map.of(
					Route.DATASOURCE, Authorization.OAUTH2,
					Route.SEARCHER, Authorization.OAUTH2))
				.build(),

			TenantManagementEvent.TenantCreated.builder()
				.tenantId("alabasta")
				.schemaName("alabasta")
				.hostName("alabasta.localhost")
				.issuerUri("http://alabasta.localhost:9090/realms/alabasta")
				.clientId("openk9")
				.routeAuthorizationMap(Map.of(
					Route.DATASOURCE, Authorization.OAUTH2,
					Route.SEARCHER, Authorization.API_KEY))
				.build(),

			TenantManagementEvent.TenantCreated.builder()
				.tenantId("sabaody")
				.schemaName("sabaody")
				.hostName("sabaody.localhost")
				.issuerUri("http://sabaody.localhost:9090/realms/sabaody")
				.clientId("openk9")
				.routeAuthorizationMap(Map.of(
					Route.DATASOURCE, Authorization.API_KEY,
					Route.SEARCHER, Authorization.API_KEY))
				.build(),

			TenantManagementEvent.TenantCreated.builder()
				.tenantId("skypea")
				.schemaName("skypea")
				.hostName("skypea.localhost")
				.issuerUri("http://skypea.localhost:9090/realms/skypea")
				.clientId("openk9")
				.routeAuthorizationMap(Map.of())
				.build(),

			TenantManagementEvent.TenantCreated.builder()
				.tenantId("waterseven")
				.schemaName("waterseven")
				.hostName("waterseven.localhost")
				.issuerUri("http://waterseven.localhost:9090/realms/waterseven")
				.clientId("openk9")
				.build()

		);
	}

	public static List<TenantManagementEvent.ApiKeyCreated> apiKeyCreatedEvents() {
		return List.of(
			// Loguetown API keys
			TenantManagementEvent.ApiKeyCreated.builder()
				.tenantId("loguetown")
				.apiKeyHash("54a0fac2b6c83ac31a1af4a33b81f80e67659f3f4b3083d6f36ac04cd376e374")
				.checksum("a9d05b90")
				.build(),
			TenantManagementEvent.ApiKeyCreated.builder()
				.tenantId("loguetown")
				.apiKeyHash("05a4f803d7b05d2308c586494db1d869eb14f1629dbda655fb62df72cfac75f6")
				.checksum("4679e9c9")
				.build(),

			// Drum API key
			TenantManagementEvent.ApiKeyCreated.builder()
				.tenantId("drum")
				.apiKeyHash("54a0fac2b6c83ac31a1af4a33b81f80e67659f3f4b3083d6f36ac04cd376e374")
				.checksum("a9d05b90")
				.build(),

			// Alabasta API key
			TenantManagementEvent.ApiKeyCreated.builder()
				.tenantId("alabasta")
				.apiKeyHash("54a0fac2b6c83ac31a1af4a33b81f80e67659f3f4b3083d6f36ac04cd376e374")
				.checksum("a9d05b90")
				.build(),

			// Sabaody API keys
			TenantManagementEvent.ApiKeyCreated.builder()
				.tenantId("sabaody")
				.apiKeyHash("54a0fac2b6c83ac31a1af4a33b81f80e67659f3f4b3083d6f36ac04cd376e374")
				.checksum("a9d05b90")
				.build(),
			TenantManagementEvent.ApiKeyCreated.builder()
				.tenantId("sabaody")
				.apiKeyHash("dc94fddbae55ea98995b28eca222f934de35bc05eb8af91cc6956c0c3dcc233f")
				.checksum("bd0a32e2")
				.build()
		);
	}
}
