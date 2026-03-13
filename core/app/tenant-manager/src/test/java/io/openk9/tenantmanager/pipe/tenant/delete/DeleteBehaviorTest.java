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

package io.openk9.tenantmanager.pipe.tenant.delete;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.DeleteAllResourcesResponse;
import io.openk9.quarkus.common.EventBusInstanceHolder;
import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.SecurityConfiguration;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteMessage;
import io.openk9.tenantmanager.service.TenantProvisioningService;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeleteBehaviorTest {

	private EventBus eventBus;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		eventBus = mock(EventBus.class);
		EventBusInstanceHolder.setEventBus(eventBus);
	}

	@Test
	@SuppressWarnings("unchecked")
	@DisplayName(
		"deleteRealm is called when realmProvisioned is true")
	void deleteBehavior_deletesRealmWhenProvisioned() {

		// Arrange
		TenantResponseDTO tenant = new TenantResponseDTO(
			"42", "schema1", "schema1-liq", "host1.local",
			"cid", "csec", "issuer",
			SecurityConfiguration.LEGACY, true);

		mockFindTenant("host1.local", tenant);
		mockDeleteSchema();
		mockDeleteRealm();
		mockDeleteEntity();
		mockDeleteIngress();

		AppManager appManager = mock(AppManager.class);
		when(appManager.deleteAllResources(any()))
			.thenReturn(Uni.createFrom().item(
				DeleteAllResourcesResponse.newBuilder().build()));

		TypedActor.Address<DeleteMessage> noopSelf = msg -> {};
		DeleteBehavior behavior =
			new DeleteBehavior(eventBus, noopSelf);

		// Act: Send Start to initialise virtualHost and capture token
		behavior.apply(new DeleteMessage.Start(null, "host1.local"));
		String token = behavior.getToken();

		behavior.apply(new DeleteMessage.Delete(token, appManager, "1.0"));

		// Assert: DELETE_REALM was called
		verify(eventBus).request(
			eq(TenantProvisioningService.DELETE_REALM), any());
	}

	@Test
	@SuppressWarnings("unchecked")
	@DisplayName(
		"deleteRealm is NOT called when realmProvisioned is false")
	void deleteBehavior_skipsRealmWhenNotProvisioned() {

		// Arrange
		TenantResponseDTO tenant = new TenantResponseDTO(
			"43", "schema2", "schema2-liq", "host2.local",
			null, null, null,
			SecurityConfiguration.BASIC_AUTH, false);

		mockFindTenant("host2.local", tenant);
		mockDeleteSchema();
		mockDeleteEntity();
		mockDeleteIngress();

		AppManager appManager = mock(AppManager.class);
		when(appManager.deleteAllResources(any()))
			.thenReturn(Uni.createFrom().item(
				DeleteAllResourcesResponse.newBuilder().build()));

		TypedActor.Address<DeleteMessage> noopSelf = msg -> {};
		DeleteBehavior behavior =
			new DeleteBehavior(eventBus, noopSelf);

		// Act: Send Start to initialise virtualHost and capture token
		behavior.apply(new DeleteMessage.Start(null, "host2.local"));
		String token = behavior.getToken();

		behavior.apply(new DeleteMessage.Delete(token, appManager, "1.0"));

		// Assert: DELETE_REALM was NOT called
		verify(eventBus, never()).request(
			eq(TenantProvisioningService.DELETE_REALM), any());
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void mockFindTenant(String virtualHost, TenantResponseDTO dto) {
		Message msg = mock(Message.class);
		when(msg.body()).thenReturn(dto);
		when(eventBus.request(
			eq(TenantProvisioningService.FIND_TENANT_BY_VIRTUAL_HOST), any()))
			.thenReturn(Uni.createFrom().item(msg));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void mockVoidEventBusCall(String address) {
		Message msg = mock(Message.class);
		when(msg.body()).thenReturn(null);
		when(eventBus.request(eq(address), any()))
			.thenReturn(Uni.createFrom().item(msg));
	}

	private void mockDeleteSchema() {
		mockVoidEventBusCall(TenantProvisioningService.DELETE_SCHEMA);
	}

	private void mockDeleteRealm() {
		mockVoidEventBusCall(TenantProvisioningService.DELETE_REALM);
	}

	private void mockDeleteEntity() {
		mockVoidEventBusCall(TenantProvisioningService.DELETE_ENTITY);
	}

	private void mockDeleteIngress() {
		mockVoidEventBusCall(TenantProvisioningService.DELETE_INGRESS);
	}

}
