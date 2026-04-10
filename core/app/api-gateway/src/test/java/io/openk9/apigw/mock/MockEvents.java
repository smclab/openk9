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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import io.openk9.event.tenant.ApiGroup;
import io.openk9.event.tenant.AuthorizationScheme;
import io.openk9.event.tenant.TenantEvent;

public class MockEvents {

	public static List<TenantEvent.TenantCreated> tenantCreatedEvents() {
		return List.of(

			TenantEvent.TenantCreated.builder()
				.tenantId("loguetown")
				.tenantName("loguetown")
				.hostName("loguetown.localhost")
				.routeAuthorizationMap(Map.of(
					ApiGroup.ADMINISTRATION, AuthorizationScheme.API_KEY
				))
				.build(),

			TenantEvent.TenantCreated.builder()
				.tenantId("drum")
				.tenantName("drum")
				.hostName("drum.localhost")
				.issuerUri("http://drum.localhost:9090/realms/drum")
				.clientId("openk9")
				.routeAuthorizationMap(Map.of(
					ApiGroup.ADMINISTRATION, AuthorizationScheme.OAUTH2,
					ApiGroup.SEARCH, AuthorizationScheme.OAUTH2))
				.build(),

			TenantEvent.TenantCreated.builder()
				.tenantId("alabasta")
				.tenantName("alabasta")
				.hostName("alabasta.localhost")
				.issuerUri("http://alabasta.localhost:9090/realms/alabasta")
				.clientId("openk9")
				.routeAuthorizationMap(Map.of(
					ApiGroup.ADMINISTRATION, AuthorizationScheme.OAUTH2,
					ApiGroup.SEARCH, AuthorizationScheme.API_KEY))
				.build(),

			TenantEvent.TenantCreated.builder()
				.tenantId("sabaody")
				.tenantName("sabaody")
				.hostName("sabaody.localhost")
				.issuerUri("http://sabaody.localhost:9090/realms/sabaody")
				.clientId("openk9")
				.routeAuthorizationMap(Map.of(
					ApiGroup.ADMINISTRATION, AuthorizationScheme.API_KEY,
					ApiGroup.SEARCH, AuthorizationScheme.API_KEY,
					ApiGroup.INGESTION, AuthorizationScheme.API_KEY))
				.build(),

			TenantEvent.TenantCreated.builder()
				.tenantId("skypea")
				.tenantName("skypea")
				.hostName("skypea.localhost")
				.issuerUri("http://skypea.localhost:9090/realms/skypea")
				.clientId("openk9")
				.routeAuthorizationMap(Map.of())
				.build(),

			TenantEvent.TenantCreated.builder()
				.tenantId("waterseven")
				.tenantName("waterseven")
				.hostName("waterseven.localhost")
				.issuerUri("http://waterseven.localhost:9090/realms/waterseven")
				.clientId("openk9")
				.build()

		);
	}

	public static List<TenantEvent.ApiKeyCreated> apiKeyCreatedEvents() {
		return List.of(
			// Loguetown API keys
			TenantEvent.ApiKeyCreated.builder()
				.tenantId("loguetown")
				.apiKeyHash("54a0fac2b6c83ac31a1af4a33b81f80e67659f3f4b3083d6f36ac04cd376e374")
				.checksum("a9d05b90")
				.apiGroup(ApiGroup.ADMINISTRATION)
				.expirationDate(OffsetDateTime.now().plusYears(1))
				.build(),
			TenantEvent.ApiKeyCreated.builder()
				.tenantId("loguetown")
				.apiKeyHash("05a4f803d7b05d2308c586494db1d869eb14f1629dbda655fb62df72cfac75f6")
				.checksum("4679e9c9")
				.apiGroup(ApiGroup.ADMINISTRATION)
				.expirationDate(OffsetDateTime.now().plusYears(1))
				.build(),

			// Drum API key
			TenantEvent.ApiKeyCreated.builder()
				.tenantId("drum")
				.apiKeyHash("54a0fac2b6c83ac31a1af4a33b81f80e67659f3f4b3083d6f36ac04cd376e374")
				.checksum("a9d05b90")
				.apiGroup(ApiGroup.ADMINISTRATION)
				.expirationDate(OffsetDateTime.now().plusYears(1))
				.build(),

			// Alabasta API key (SEARCH — used on searcher routes)
			TenantEvent.ApiKeyCreated.builder()
				.tenantId("alabasta")
				.apiKeyHash("54a0fac2b6c83ac31a1af4a33b81f80e67659f3f4b3083d6f36ac04cd376e374")
				.checksum("a9d05b90")
				.apiGroup(ApiGroup.SEARCH)
				.expirationDate(OffsetDateTime.now().plusYears(1))
				.build(),

			// Sabaody: ADMINISTRATION key (for datasource routes)
			TenantEvent.ApiKeyCreated.builder()
				.tenantId("sabaody")
				.apiKeyHash("54a0fac2b6c83ac31a1af4a33b81f80e67659f3f4b3083d6f36ac04cd376e374")
				.checksum("a9d05b90")
				.apiGroup(ApiGroup.ADMINISTRATION)
				.expirationDate(OffsetDateTime.now().plusYears(1))
				.build(),
			// Sabaody: SEARCH key (for searcher routes)
			TenantEvent.ApiKeyCreated.builder()
				.tenantId("sabaody")
				.apiKeyHash("dc94fddbae55ea98995b28eca222f934de35bc05eb8af91cc6956c0c3dcc233f")
				.checksum("bd0a32e2")
				.apiGroup(ApiGroup.SEARCH)
				.expirationDate(OffsetDateTime.now().plusYears(1))
				.build(),

			// Sabaody: INGESTION key (for ApiGroup tests)
			TenantEvent.ApiKeyCreated.builder()
				.tenantId("sabaody")
				.apiKeyHash("ce9acc4d057d2c94788c2943d1b09079e594f7965a92763f285a31ebfda19d26")
				.checksum("f079ba95")
				.apiGroup(ApiGroup.INGESTION)
				.expirationDate(OffsetDateTime.now().plusYears(1))
				.build(),

			// Sabaody: expired SEARCH key (for expiration tests)
			TenantEvent.ApiKeyCreated.builder()
				.tenantId("sabaody")
				.apiKeyHash("8a92ca186d01804f7766bff613df9613996b868c6ee975eca040a269d7fcccff")
				.checksum("d4504e06")
				.apiGroup(ApiGroup.SEARCH)
				.expirationDate(OffsetDateTime.now().minusHours(1))
				.build()
		);
	}
}
