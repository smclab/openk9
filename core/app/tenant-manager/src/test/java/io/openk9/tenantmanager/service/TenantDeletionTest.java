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

package io.openk9.tenantmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.DeleteAllResourcesResponse;
import io.openk9.app.manager.grpc.DeleteIngressResponse;
import io.openk9.quarkus.common.EventBusInstanceHolder;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.SecurityConfiguration;
import io.openk9.tenantmanager.service.dto.DeleteTenantRequest;
import io.openk9.tenantmanager.service.dto.DeleteTenantResponse;
import io.openk9.tenantmanager.service.dto.EffectiveDeleteTenantRequest;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Tenant deletion")
class TenantDeletionTest {

	private TenantProvisioningService service;
	private TenantDbService dbService;
	private TenantSchemaService schemaService;
	private TenantRealmService realmService;
	private AppManager appManager;

	@BeforeEach
	void setUp() {
		dbService = mock(TenantDbService.class);
		schemaService = mock(TenantSchemaService.class);
		realmService = mock(TenantRealmService.class);
		appManager = mock(AppManager.class);

		// EventBus mock for fire-and-forget dispatch
		EventBus eventBus = mock(EventBus.class);
		EventBusInstanceHolder.setEventBus(eventBus);

		service = new TenantProvisioningService();
		service.applicationVersion = "1.0";
		service.dbService = dbService;
		service.schemaService = schemaService;
		service.realmService = realmService;
		service.appManagerService = appManager;

		// default mocks
		when(appManager.deleteAllResources(any()))
			.thenReturn(Uni.createFrom().item(
				DeleteAllResourcesResponse.newBuilder()
					.build()));
		when(appManager.deleteIngress(any()))
			.thenReturn(Uni.createFrom().item(
				DeleteIngressResponse.newBuilder()
					.build()));
		when(dbService.deleteTenant(anyLong()))
			.thenReturn(Uni.createFrom().voidItem());
	}

	@Nested
	@DisplayName("executeDeletion()")
	class ExecuteDeletion {

		@Test
		@DisplayName("calls deleteRealm when realmProvisioned")
		void deletesRealmWhenProvisioned() {
			// arrange
			var tenant = tenantWith(
				"42", "schema1", "host1.local",
				SecurityConfiguration.LEGACY, true);

			when(dbService.findByVirtualHost("host1.local"))
				.thenReturn(Uni.createFrom().item(tenant));
			when(realmService.deleteRealm(anyString()))
				.thenReturn(Uni.createFrom().voidItem());

			// act
			service.executeDeletion("host1.local");

			// assert
			verify(realmService).deleteRealm("schema1");
		}

		@Test
		@DisplayName("skips deleteRealm when not provisioned")
		void skipsRealmWhenNotProvisioned() {
			// arrange
			var tenant = tenantWith(
				"43", "schema2", "host2.local",
				SecurityConfiguration.BASIC_AUTH, false);

			when(dbService.findByVirtualHost("host2.local"))
				.thenReturn(Uni.createFrom().item(tenant));

			// act
			service.executeDeletion("host2.local");

			// assert
			verify(realmService, never())
				.deleteRealm(anyString());
		}

		@Test
		@DisplayName(
			"collects errors when deleteRealm fails")
		void continuesWhenRealmDeleteFails() {
			// arrange
			var tenant = tenantWith(
				"44", "schema3", "host3.local",
				SecurityConfiguration.LEGACY, true);

			when(dbService.findByVirtualHost("host3.local"))
				.thenReturn(Uni.createFrom().item(tenant));
			when(realmService.deleteRealm(anyString()))
				.thenReturn(Uni.createFrom().failure(
					new RuntimeException("realm failed")));

			// act — errors are logged, not thrown
			service.executeDeletion("host3.local");

			// assert: other operations still called
			verify(dbService).deleteTenant(44L);
		}

		@Test
		@DisplayName(
			"collects errors when deleteTenant fails")
		void continuesWhenEntityDeleteFails() {
			// arrange
			var tenant = tenantWith(
				"45", "schema4", "host4.local",
				SecurityConfiguration.BASIC_AUTH, false);

			when(dbService.findByVirtualHost("host4.local"))
				.thenReturn(Uni.createFrom().item(tenant));
			when(dbService.deleteTenant(45L))
				.thenReturn(Uni.createFrom().failure(
					new RuntimeException("entity failed")));

			// act — errors are logged, not thrown
			service.executeDeletion("host4.local");

			// assert: ingress deletion still called
			verify(appManager).deleteIngress(any());
		}
	}

	@Nested
	@DisplayName("Token flow")
	class TokenFlow {

		@Test
		@DisplayName(
			"requestDeletion returns a token in response")
		void requestDeletionReturnsToken() {
			// arrange
			when(dbService.findByVirtualHost("host5.local"))
				.thenReturn(Uni.createFrom().item(
					tenantWith("50", "schema5",
						"host5.local",
						SecurityConfiguration.BASIC_AUTH,
						false)));

			// act
			DeleteTenantResponse response = service
				.requestDeletion(
					new DeleteTenantRequest("host5.local"))
				.subscribe()
				.withSubscriber(UniAssertSubscriber.create())
				.awaitItem()
				.getItem();

			// assert
			assertNotNull(response.message());
		}

		@Test
		@DisplayName(
			"delete with valid token starts deletion")
		void deleteWithValidToken() {
			// arrange
			var tenant = tenantWith(
				"51", "schema6", "host6.local",
				SecurityConfiguration.BASIC_AUTH, false);

			when(dbService.findByVirtualHost("host6.local"))
				.thenReturn(Uni.createFrom().item(tenant));

			// act: request token
			DeleteTenantResponse requestResp = service
				.requestDeletion(
					new DeleteTenantRequest("host6.local"))
				.subscribe()
				.withSubscriber(UniAssertSubscriber.create())
				.awaitItem()
				.getItem();

			String token = requestResp.message();

			// act: confirm deletion
			DeleteTenantResponse deleteResp = service
				.delete(
					new EffectiveDeleteTenantRequest(
							"host6.local", token))
				.subscribe()
				.withSubscriber(UniAssertSubscriber.create())
				.awaitItem()
				.getItem();

			// assert
			assertEquals(
				"delete tenant started",
				deleteResp.message());
		}

		@Test
		@DisplayName(
			"delete with invalid token is rejected")
		void deleteWithInvalidToken() {
			// arrange
			when(dbService.findByVirtualHost("host7.local"))
				.thenReturn(Uni.createFrom().item(
					tenantWith("52", "schema7",
						"host7.local",
						SecurityConfiguration.BASIC_AUTH,
						false)));

			// request a token
			service.requestDeletion(
					new DeleteTenantRequest("host7.local"))
				.subscribe()
				.withSubscriber(UniAssertSubscriber.create())
				.awaitItem();

			// act: confirm with wrong token
			service.delete(
					new EffectiveDeleteTenantRequest(
							"host7.local", "wrong-token"))
				.subscribe()
				.withSubscriber(UniAssertSubscriber.create())
				.awaitFailure()
				.assertFailed();
		}
	}

	// -----------------------------------------------------------------
	// Helpers
	// -----------------------------------------------------------------

	private static TenantResponseDTO tenantWith(
		String id, String schemaName, String virtualHost,
		SecurityConfiguration secConfig,
		boolean realmProvisioned) {

		return new TenantResponseDTO(
			id, schemaName, schemaName + "-liq",
			virtualHost, null, null, null,
			secConfig, realmProvisioned);
	}
}
