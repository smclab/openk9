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
package io.openk9.tenantmanager.pipe.tenant.create;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.openk9.app.manager.grpc.IngressScope;
import io.openk9.quarkus.common.EventBusInstanceHolder;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.SecurityConfiguration;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.TenantProvisioningService;
import io.openk9.tenantmanager.service.TenantProvisioningService.CreateEntityRequest;
import io.openk9.tenantmanager.service.TenantRealmService;
import io.openk9.tenantmanager.service.dto.OAuth2Settings;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TenantProvisioningSagaTest {

	private static final ActorTestKit testKit = ActorTestKit.create();
	private EventBus eventBus;

	@AfterAll
	public static void cleanup() {
		testKit.shutdownTestKit();
	}

	@BeforeEach
	void setUp() {
		eventBus = mock(EventBus.class);
		EventBusInstanceHolder.setEventBus(eventBus);
	}


	@Test
	void should_compensate_when_initial_step_fails() {
		TenantRealmService.setKeycloakAvailable(true);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vhost", "schema",
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			replyTo.getRef(),
			mocks
		));

		// 1. Simulate Parallel Execution
		// Realm Fails
		mocks.realmProbe.expectMessage(
			RealmProvisioner.Start.INSTANCE);
		mocks.realmAdapter.get().tell(
			new RealmProvisioner.Error("Network Error"));

		// Schema Succeeds
		mocks.schemaProbe.expectMessage(
			SchemaProvisioner.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new SchemaProvisioner.Success("schema"));

		// Ingress Succeeds
		mocks.ingressProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);

		// 2. Expect Compensations for the Successes
		// (Schema & Ingress)

		// --- Schema Rollback ---
		mocks.schemaRollbackProbe.expectMessage(
			SchemaProvisioner.Rollback.INSTANCE);
		mocks.schemaRollbackAdapter.get().tell(
			new SchemaProvisioner.Success("Rolled back"));

		// --- Ingress Rollback ---
		mocks.ingressRollbackProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressRollbackAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);

		// 3. Verify Final Saga Error
		replyTo.expectMessage(
			TenantProvisioningSaga.Error.INSTANCE);
	}

	@Test
	@SuppressWarnings("unchecked")
	void should_succeed_when_all_children_succeed() {
		TenantRealmService.setKeycloakAvailable(true);

		TenantResponseDTO expectedTenant = new TenantResponseDTO(
			"1213402949",
			"mySchema",
			"vHost",
			"cid",
			"sec",
			"issuer",
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			true);

		Message<TenantResponseDTO> mockMsg = mock(Message.class);
		when(mockMsg.body()).thenReturn(expectedTenant);

		// Explicit cast to help generic inference
		Uni<Message<TenantResponseDTO>> uni =
			Uni.createFrom().item(mockMsg);
		when(eventBus.<TenantResponseDTO>request(
			eq(TenantProvisioningService.CREATE_ENTITY), any()))
			.thenReturn(uni);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vhost", "schema",
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			replyTo.getRef(),
			mocks));

		// 1. Verify Parallel Starts and Reply Success
		mocks.realmProbe.expectMessage(
			RealmProvisioner.Start.INSTANCE);
		mocks.realmAdapter.get().tell(
			new RealmProvisioner.Success(
				"cid", "sec", "vhost", "iss", "usr", "pwd"));

		mocks.schemaProbe.expectMessage(
			SchemaProvisioner.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new SchemaProvisioner.Success("schema"));

		mocks.ingressProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);

		// 2. Verify Final Saga Response
		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void should_succeed_with_oauth2_settings() {
		TenantRealmService.setKeycloakAvailable(false);

		TenantResponseDTO expectedTenant = new TenantResponseDTO(
			"1213402949",
			"mySchema",
			"vHost",
			"cid",
			"sec",
			"issuer",
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			false);

		Message<TenantResponseDTO> mockMsg = mock(Message.class);
		when(mockMsg.body()).thenReturn(expectedTenant);

		Uni<Message<TenantResponseDTO>> uni =
			Uni.createFrom().item(mockMsg);
		when(eventBus.<TenantResponseDTO>request(
			eq(TenantProvisioningService.CREATE_ENTITY), any()))
			.thenReturn(uni);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();
		OAuth2Settings settings =
			new OAuth2Settings("cid", "csec", "issuer");

		testKit.spawn(TenantProvisioningSaga.create(
			"vh", "tenant", settings,
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			null,
			replyTo.getRef(),
			mocks)
		);

		// 1. Verify Parallel Starts
		// (Realm skipped due to OAuth2Settings)
		mocks.schemaProbe.expectMessage(
			SchemaProvisioner.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new SchemaProvisioner.Success("schema"));

		mocks.ingressProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);

		// Realm should NOT start
		mocks.realmProbe.expectNoMessage();

		// 2. Verify Final Saga Response
		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void should_generate_name_when_null() {
		TenantRealmService.setKeycloakAvailable(true);

		// Mock name generation
		Message<String> nameMsg = mock(Message.class);
		when(nameMsg.body()).thenReturn("generatedschema");

		Uni<Message<String>> nameUni =
			Uni.createFrom().item(nameMsg);
		when(eventBus.<String>request(
			eq(TenantProvisioningService
				.GENERATE_RANDOM_TENANT_NAME),
			any())
		).thenReturn(nameUni);

		TenantResponseDTO expectedTenant = new TenantResponseDTO(
			"1213402949",
			"generatedschema",
			"vHost",
			"cid",
			"sec",
			"issuer",
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			true);

		Message<TenantResponseDTO> tenantMsg = mock(Message.class);
		when(tenantMsg.body()).thenReturn(expectedTenant);

		Uni<Message<TenantResponseDTO>> tenantUni =
			Uni.createFrom().item(tenantMsg);
		when(eventBus.<TenantResponseDTO>request(
			eq(TenantProvisioningService.CREATE_ENTITY),
			any()))
		.thenReturn(tenantUni);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vh", null,
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			replyTo.getRef(), mocks));

		mocks.realmProbe.expectMessage(
			RealmProvisioner.Start.INSTANCE);
		mocks.realmAdapter.get().tell(
			new RealmProvisioner.Success(
				"cid", "sec", "vhost", "iss", "usr", "pwd"));

		mocks.schemaProbe.expectMessage(
			SchemaProvisioner.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new SchemaProvisioner.Success("generatedschema"));

		mocks.ingressProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);

		// 3. Verify Final Saga Response
		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void should_succeed_without_realm() {
		TenantRealmService.setKeycloakAvailable(false);

		TenantResponseDTO expectedTenant = new TenantResponseDTO(
			"1213402949",
			"mySchema",
			"vHost",
			null,
			null,
			null,
			SecurityConfiguration.NO_GATEWAY_AUTH,
			false);

		Message<TenantResponseDTO> mockMsg = mock(Message.class);
		when(mockMsg.body()).thenReturn(expectedTenant);

		Uni<Message<TenantResponseDTO>> uni =
			Uni.createFrom().item(mockMsg);
		when(eventBus.<TenantResponseDTO>request(
			eq(TenantProvisioningService.CREATE_ENTITY), any()))
			.thenReturn(uni);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vh", "tenant",
			SecurityConfiguration.NO_GATEWAY_AUTH,
			replyTo.getRef(),
			mocks)
		);

		// Realm skipped (NO_GATEWAY_AUTH)
		mocks.schemaProbe.expectMessage(
			SchemaProvisioner.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new SchemaProvisioner.Success("schema"));

		mocks.ingressProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);

		// Realm should NOT start
		mocks.realmProbe.expectNoMessage();

		// Verify Final Saga Response
		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void should_skip_realm_for_basic_auth_even_with_keycloak() {
		TenantRealmService.setKeycloakAvailable(true);

		TenantResponseDTO expectedTenant = new TenantResponseDTO(
			"1213402949",
			"mySchema",
			"vHost",
			null,
			null,
			null,
			SecurityConfiguration.NO_GATEWAY_AUTH,
			false);

		Message<TenantResponseDTO> mockMsg = mock(Message.class);
		when(mockMsg.body()).thenReturn(expectedTenant);

		Uni<Message<TenantResponseDTO>> uni =
			Uni.createFrom().item(mockMsg);
		when(eventBus.<TenantResponseDTO>request(
			eq(TenantProvisioningService.CREATE_ENTITY), any()))
			.thenReturn(uni);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vh", "tenant",
			SecurityConfiguration.NO_GATEWAY_AUTH,
			replyTo.getRef(),
			mocks)
		);

		// Realm skipped (NO_GATEWAY_AUTH even with keycloakAvailable=true)
		mocks.schemaProbe.expectMessage(
			SchemaProvisioner.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new SchemaProvisioner.Success("schema"));

		mocks.ingressProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);

		mocks.realmProbe.expectNoMessage();

		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);
	}

	@Test
	void should_fail_fast_without_realm_for_oauth2_config() {
		TenantRealmService.setKeycloakAvailable(false);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vh", "tenant",
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			replyTo.getRef(),
			mocks)
		);

		// Should fail: no Keycloak, no oAuth2Settings, requires OAuth2
		replyTo.expectMessage(
			TenantProvisioningSaga.Error.INSTANCE);

		mocks.realmProbe.expectNoMessage();
		mocks.schemaProbe.expectNoMessage();
		mocks.ingressProbe.expectNoMessage();
	}

	// --- realmProvisioned flag tests ---

	@Test
	@SuppressWarnings("unchecked")
	@DisplayName(
		"realmProvisioned is true when Keycloak auto-provisions the realm")
	void realmProvisioned_isTrueWhenKeycloakAutoProvisions() {

		// Arrange: Keycloak is configured, no oAuth2Settings, OAUTH2_ADMIN_ONLY
		TenantRealmService.setKeycloakAvailable(true);

		ArgumentCaptor<CreateEntityRequest> captor =
			ArgumentCaptor.forClass(CreateEntityRequest.class);
		TenantResponseDTO stubDto = new TenantResponseDTO(
			"1", "s", "vh", null, null, null,
			SecurityConfiguration.OAUTH2_ADMIN_ONLY, true);

		Message<TenantResponseDTO> mockMsg = mock(Message.class);
		when(mockMsg.body()).thenReturn(stubDto);
		Uni<Message<TenantResponseDTO>> uni =
			Uni.createFrom().item(mockMsg);
		when(eventBus.<TenantResponseDTO>request(
			eq(TenantProvisioningService.CREATE_ENTITY),
			captor.capture()))
			.thenReturn(uni);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		// Act
		testKit.spawn(TenantProvisioningSaga.create(
			"vh", "schema",
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			replyTo.getRef(),
			mocks));

		mocks.realmProbe.expectMessage(
			RealmProvisioner.Start.INSTANCE);
		mocks.realmAdapter.get().tell(
			new RealmProvisioner.Success(
				"cid", "sec", "vh", "iss", null, null));
		mocks.schemaProbe.expectMessage(
			SchemaProvisioner.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new SchemaProvisioner.Success("schema"));
		mocks.ingressProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);

		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);

		// Assert: captured tenant has realmProvisioned = true
		Tenant tenant = captor.getValue().tenant();
		assertTrue(tenant.isRealmProvisioned());
	}

	@Test
	@SuppressWarnings("unchecked")
	@DisplayName(
		"realmProvisioned is false when external OAuth2 settings are used")
	void realmProvisioned_isFalseWithExternalOAuth2() {

		// Arrange: oAuth2Settings provided, no Keycloak realm created
		TenantRealmService.setKeycloakAvailable(false);

		ArgumentCaptor<CreateEntityRequest> captor =
			ArgumentCaptor.forClass(CreateEntityRequest.class);
		TenantResponseDTO stubDto = new TenantResponseDTO(
			"1", "s", "vh", "cid", "csec", "iss",
			SecurityConfiguration.OAUTH2_ADMIN_ONLY, false);

		Message<TenantResponseDTO> mockMsg = mock(Message.class);
		when(mockMsg.body()).thenReturn(stubDto);
		Uni<Message<TenantResponseDTO>> uni =
			Uni.createFrom().item(mockMsg);
		when(eventBus.<TenantResponseDTO>request(
			eq(TenantProvisioningService.CREATE_ENTITY),
			captor.capture()))
			.thenReturn(uni);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();
		OAuth2Settings settings =
			new OAuth2Settings("cid", "csec", "https://idp/issuer");

		// Act
		testKit.spawn(TenantProvisioningSaga.create(
			"vh", "schema", settings,
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			null,
			replyTo.getRef(),
			mocks));

		mocks.schemaProbe.expectMessage(
			SchemaProvisioner.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new SchemaProvisioner.Success("schema"));
		mocks.ingressProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);
		mocks.realmProbe.expectNoMessage();

		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);

		// Assert: captured tenant has realmProvisioned = false
		Tenant tenant = captor.getValue().tenant();
		assertFalse(tenant.isRealmProvisioned());
	}

	@Test
	@SuppressWarnings("unchecked")
	@DisplayName(
		"realmProvisioned is false when securityConfiguration is NO_GATEWAY_AUTH")
	void realmProvisioned_isFalseWithBasicAuth() {

		// Arrange: NO_GATEWAY_AUTH — no realm regardless of Keycloak
		TenantRealmService.setKeycloakAvailable(true);

		ArgumentCaptor<CreateEntityRequest> captor =
			ArgumentCaptor.forClass(CreateEntityRequest.class);
		TenantResponseDTO stubDto = new TenantResponseDTO(
			"1", "s", "vh", null, null, null,
			SecurityConfiguration.NO_GATEWAY_AUTH, false);

		Message<TenantResponseDTO> mockMsg = mock(Message.class);
		when(mockMsg.body()).thenReturn(stubDto);
		Uni<Message<TenantResponseDTO>> uni =
			Uni.createFrom().item(mockMsg);
		when(eventBus.<TenantResponseDTO>request(
			eq(TenantProvisioningService.CREATE_ENTITY),
			captor.capture()))
			.thenReturn(uni);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		// Act
		testKit.spawn(TenantProvisioningSaga.create(
			"vh", "schema",
			SecurityConfiguration.NO_GATEWAY_AUTH,
			replyTo.getRef(),
			mocks));

		mocks.schemaProbe.expectMessage(
			SchemaProvisioner.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new SchemaProvisioner.Success("schema"));
		mocks.ingressProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);
		mocks.realmProbe.expectNoMessage();

		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);

		// Assert: captured tenant has realmProvisioned = false
		Tenant tenant = captor.getValue().tenant();
		assertFalse(tenant.isRealmProvisioned());
	}

	@Test
	@DisplayName("rejects invalid tenant name before provisioning")
	void should_reject_invalid_tenant_name() {
		TenantRealmService.setKeycloakAvailable(true);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vh", "my-tenant",
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			replyTo.getRef(),
			mocks)
		);

		// Should fail due to invalid name (hyphen)
		replyTo.expectMessage(
			TenantProvisioningSaga.Error.INSTANCE);

		// No actors should have started
		mocks.realmProbe.expectNoMessage();
		mocks.schemaProbe.expectNoMessage();
		mocks.ingressProbe.expectNoMessage();
	}

	@Test
	@DisplayName("compensates Realm and Ingress when Schema fails")
	void should_compensate_when_schema_fails() {
		TenantRealmService.setKeycloakAvailable(true);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vhost", "schema",
			SecurityConfiguration.OAUTH2_ADMIN_ONLY,
			replyTo.getRef(),
			mocks
		));

		// 1. Simulate Parallel Execution
		// Realm Succeeds
		mocks.realmProbe.expectMessage(
			RealmProvisioner.Start.INSTANCE);
		mocks.realmAdapter.get().tell(
			new RealmProvisioner.Success(
				"cid", "sec", "vhost", "iss", "usr", "pwd"));

		// Schema Fails
		mocks.schemaProbe.expectMessage(
			SchemaProvisioner.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new SchemaProvisioner.Error("CREATE SCHEMA failed"));

		// Ingress Succeeds
		mocks.ingressProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);

		// 2. Expect Compensations for the Successes
		// (Realm & Ingress)

		// --- Realm Rollback ---
		mocks.realmRollbackProbe.expectMessage(
			RealmProvisioner.Rollback.INSTANCE);
		mocks.realmRollbackAdapter.get().tell(
			new RealmProvisioner.Success(
				null, null, null, null, null, null));

		// --- Ingress Rollback ---
		mocks.ingressRollbackProbe.expectMessage(
			IngressProvisioner.Start.INSTANCE);
		mocks.ingressRollbackAdapter.get().tell(
			IngressProvisioner.Success.INSTANCE);

		// 3. Verify Final Saga Error
		replyTo.expectMessage(
			TenantProvisioningSaga.Error.INSTANCE);
	}

	// --- Mock Factory Helper ---

	static class MockProvisioningFactory
		implements TenantProvisioningSaga.ProvisioningFactory {

		// Creation Probes & Adapters
		final TestProbe<RealmProvisioner.Command> realmProbe =
			testKit.createTestProbe();
		final AtomicReference<ActorRef<RealmProvisioner.Response>>
			realmAdapter = new AtomicReference<>();

		final TestProbe<SchemaProvisioner.Command> schemaProbe =
			testKit.createTestProbe();
		final AtomicReference<ActorRef<SchemaProvisioner.Response>>
			schemaAdapter = new AtomicReference<>();

		final TestProbe<IngressProvisioner.Command> ingressProbe =
			testKit.createTestProbe();
		final AtomicReference<ActorRef<IngressProvisioner.Response>>
			ingressAdapter = new AtomicReference<>();

		// Rollback Probes & Adapters
		final TestProbe<RealmProvisioner.Command>
			realmRollbackProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<RealmProvisioner.Response>>
			realmRollbackAdapter = new AtomicReference<>();

		final TestProbe<SchemaProvisioner.Command>
			schemaRollbackProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<SchemaProvisioner.Response>>
			schemaRollbackAdapter = new AtomicReference<>();

		final TestProbe<IngressProvisioner.Command>
			ingressRollbackProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<IngressProvisioner.Response>>
			ingressRollbackAdapter = new AtomicReference<>();

		// --- Implementation ---

		@Override
		public Behavior<IngressProvisioner.Command> ingress(
			String s, String v,
			List<IngressScope> ingressScopes,
			ActorRef<IngressProvisioner.Response> r) {

			ingressAdapter.set(r);
			return Behaviors.monitor(
				IngressProvisioner.Command.class,
				ingressProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<IngressProvisioner.Command> ingressRollback(
			String s, String v,
			ActorRef<IngressProvisioner.Response> r) {

			ingressRollbackAdapter.set(r);
			return Behaviors.monitor(
				IngressProvisioner.Command.class,
				ingressRollbackProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<RealmProvisioner.Command> realm(
			String v, String s,
			ActorRef<RealmProvisioner.Response> r) {

			realmAdapter.set(r);
			return Behaviors.monitor(
				RealmProvisioner.Command.class,
				realmProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<RealmProvisioner.Command> realmRollback(
			String s, ActorRef<RealmProvisioner.Response> r) {

			realmRollbackAdapter.set(r);
			return Behaviors.monitor(
				RealmProvisioner.Command.class,
				realmRollbackProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<SchemaProvisioner.Command> schema(
			String v, String s,
			ActorRef<SchemaProvisioner.Response> r) {

			schemaAdapter.set(r);
			return Behaviors.monitor(
				SchemaProvisioner.Command.class,
				schemaProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<SchemaProvisioner.Command> schemaRollback(
			String s, ActorRef<SchemaProvisioner.Response> r) {

			schemaRollbackAdapter.set(r);
			return Behaviors.monitor(
				SchemaProvisioner.Command.class,
				schemaRollbackProbe.ref(),
				Behaviors.empty());
		}
	}
}
