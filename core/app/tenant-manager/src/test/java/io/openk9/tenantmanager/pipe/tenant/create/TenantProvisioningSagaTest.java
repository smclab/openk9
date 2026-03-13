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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.openk9.quarkus.common.EventBusInstanceHolder;
import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.SecurityConfiguration;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.TenantProvisioningService;
import io.openk9.tenantmanager.service.TenantProvisioningService.CreateEntityRequest;
import io.openk9.tenantmanager.service.dto.OAuth2Settings;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import java.util.concurrent.atomic.AtomicReference;
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

	@SuppressWarnings("unchecked")
	private void mockRealmConfigured(boolean configured) {
		Message<Boolean> msg = mock(Message.class);
		when(msg.body()).thenReturn(configured);
		Uni<Message<Boolean>> uni = Uni.createFrom().item(msg);
		when(eventBus.<Boolean>request(
			eq(TenantProvisioningService.IS_REALM_CONFIGURED),
			any()))
			.thenReturn(uni);
	}

	@Test
	void should_compensate_when_initial_step_fails() {
		mockRealmConfigured(true);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vhost", "schema", null,
			SecurityConfiguration.LEGACY,
			replyTo.getRef(),
			mocks
		));

		// 1. Simulate Parallel Execution
		// Realm Fails
		mocks.realmProbe.expectMessage(Realm.Start.INSTANCE);
		mocks.realmAdapter.get().tell(new Realm.Error("Network Error"));

		// Schema Succeeds
		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(new Schema.Success("schema"));

		// Ingress Succeeds
		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);

		// 2. Expect Compensations for the Successes (Schema & Ingress)

		// --- Fix: Handle Schema Rollback ---
		mocks.schemaRollbackProbe.expectMessage(
			Schema.Rollback.INSTANCE);
		mocks.schemaRollbackAdapter.get().tell(
			new Schema.Success("Rolled back"));

		// --- Fix: Handle Ingress Rollback ---
		mocks.ingressRollbackProbe.expectMessage(
			Ingress.Start.INSTANCE);
		mocks.ingressRollbackAdapter.get().tell(
			Ingress.Success.INSTANCE);

		// 3. Verify Final Saga Error
		replyTo.expectMessage(
			TenantProvisioningSaga.Error.INSTANCE);
	}

	@Test
	@SuppressWarnings("unchecked")
	void should_succeed_when_all_children_succeed() {
		mockRealmConfigured(true);

		TenantResponseDTO expectedTenant = new TenantResponseDTO(
			"1213402949",
			"mySchema",
			"mySchema",
			"vHost",
			"cid",
			"sec",
			"issuer",
			SecurityConfiguration.LEGACY,
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
			"vhost", "schema", null,
			SecurityConfiguration.LEGACY,
			replyTo.getRef(),
			mocks));

		// 1. Verify Parallel Starts and Reply Success
		mocks.realmProbe.expectMessage(Realm.Start.INSTANCE);
		mocks.realmAdapter.get().tell(
			new Realm.Success(
				"cid", "sec", "vhost", "iss", "usr", "pwd"));

		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new Schema.Success("schema"));

		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);

		// 2. Verify Final Saga Response
		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void should_succeed_with_oauth2_settings() {
		mockRealmConfigured(false);

		TenantResponseDTO expectedTenant = new TenantResponseDTO(
			"1213402949",
			"mySchema",
			"mySchema",
			"vHost",
			"cid",
			"sec",
			"issuer",
			SecurityConfiguration.LEGACY,
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
			SecurityConfiguration.LEGACY,
			replyTo.getRef(),
			mocks)
		);

		// 1. Verify Parallel Starts (Realm skipped due to OAuth2Settings)
		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new Schema.Success("schema"));

		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);

		// Realm should NOT start
		mocks.realmProbe.expectNoMessage();

		// 2. Verify Final Saga Response
		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void should_generate_name_when_null() {
		mockRealmConfigured(true);

		// Mock name generation
		Message<String> nameMsg = mock(Message.class);
		when(nameMsg.body()).thenReturn("generated-schema");

		Uni<Message<String>> nameUni =
			Uni.createFrom().item(nameMsg);
		when(eventBus.<String>request(
			eq(TenantProvisioningService
				.GENERATE_RANDOM_TENANT_NAME),
			any())
		).thenReturn(nameUni);

		TenantResponseDTO expectedTenant = new TenantResponseDTO(
			"1213402949",
			"generated-schema",
			"generated-schema",
			"vHost",
			"cid",
			"sec",
			"issuer",
			SecurityConfiguration.LEGACY,
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
			"vh", null, null,
			SecurityConfiguration.LEGACY,
			replyTo.getRef(), mocks));

		mocks.realmProbe.expectMessage(Realm.Start.INSTANCE);
		mocks.realmAdapter.get().tell(
			new Realm.Success(
				"cid", "sec", "vhost", "iss", "usr", "pwd"));

		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new Schema.Success("generated-schema"));

		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);

		// 3. Verify Final Saga Response
		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void should_succeed_without_realm() {
		mockRealmConfigured(false);

		TenantResponseDTO expectedTenant = new TenantResponseDTO(
			"1213402949",
			"mySchema",
			"mySchema",
			"vHost",
			null,
			null,
			null,
			SecurityConfiguration.BASIC_AUTH,
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
			"vh", "tenant", null,
			SecurityConfiguration.BASIC_AUTH,
			replyTo.getRef(),
			mocks)
		);

		// Realm skipped (BASIC_AUTH)
		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new Schema.Success("schema"));

		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);

		// Realm should NOT start
		mocks.realmProbe.expectNoMessage();

		// Verify Final Saga Response
		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);
	}

	@Test
	@SuppressWarnings("unchecked")
	void should_skip_realm_for_basic_auth_even_with_keycloak() {
		mockRealmConfigured(true);

		TenantResponseDTO expectedTenant = new TenantResponseDTO(
			"1213402949",
			"mySchema",
			"mySchema",
			"vHost",
			null,
			null,
			null,
			SecurityConfiguration.BASIC_AUTH,
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
			"vh", "tenant", null,
			SecurityConfiguration.BASIC_AUTH,
			replyTo.getRef(),
			mocks)
		);

		// Realm skipped (BASIC_AUTH even with realmConfigured=true)
		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(
			new Schema.Success("schema"));

		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);

		mocks.realmProbe.expectNoMessage();

		replyTo.expectMessageClass(
			TenantProvisioningSaga.Success.class);
	}

	@Test
	void should_fail_fast_without_realm_for_oauth2_config() {
		mockRealmConfigured(false);

		TestProbe<TenantProvisioningSaga.Response> replyTo =
			testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vh", "tenant", null,
			SecurityConfiguration.LEGACY,
			replyTo.getRef(),
			mocks)
		);

		// Should fail after realmConfigured resolves to false
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

		// Arrange: Keycloak is configured, no oAuth2Settings, LEGACY
		mockRealmConfigured(true);

		ArgumentCaptor<CreateEntityRequest> captor =
			ArgumentCaptor.forClass(CreateEntityRequest.class);
		TenantResponseDTO stubDto = new TenantResponseDTO(
			"1", "s", "s-liq", "vh", null, null, null,
			SecurityConfiguration.LEGACY, true);

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
			"vh", "schema", null,
			SecurityConfiguration.LEGACY,
			replyTo.getRef(),
			mocks));

		mocks.realmProbe.expectMessage(Realm.Start.INSTANCE);
		mocks.realmAdapter.get().tell(
			new Realm.Success("cid", "sec", "vh", "iss", null, null));
		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(new Schema.Success("schema"));
		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);

		replyTo.expectMessageClass(TenantProvisioningSaga.Success.class);

		// Assert: captured tenant has realmProvisioned = true
		Tenant tenant = captor.getValue().tenant();
		assertTrue(tenant.isRealmProvisioned());
	}

	@Test
	@SuppressWarnings("unchecked")
	@DisplayName("realmProvisioned is false when external OAuth2 settings are used")
	void realmProvisioned_isFalseWithExternalOAuth2() {

		// Arrange: oAuth2Settings provided, no Keycloak realm created
		mockRealmConfigured(false);

		ArgumentCaptor<CreateEntityRequest> captor =
			ArgumentCaptor.forClass(CreateEntityRequest.class);
		TenantResponseDTO stubDto = new TenantResponseDTO(
			"1", "s", "s-liq", "vh", "cid", "csec", "iss",
			SecurityConfiguration.LEGACY, false);

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
			SecurityConfiguration.LEGACY,
			replyTo.getRef(),
			mocks));

		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(new Schema.Success("schema"));
		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);
		mocks.realmProbe.expectNoMessage();

		replyTo.expectMessageClass(TenantProvisioningSaga.Success.class);

		// Assert: captured tenant has realmProvisioned = false
		Tenant tenant = captor.getValue().tenant();
		assertFalse(tenant.isRealmProvisioned());
	}

	@Test
	@SuppressWarnings("unchecked")
	@DisplayName("realmProvisioned is false when securityConfiguration is BASIC_AUTH")
	void realmProvisioned_isFalseWithBasicAuth() {

		// Arrange: BASIC_AUTH — no realm regardless of Keycloak availability
		mockRealmConfigured(true);

		ArgumentCaptor<CreateEntityRequest> captor =
			ArgumentCaptor.forClass(CreateEntityRequest.class);
		TenantResponseDTO stubDto = new TenantResponseDTO(
			"1", "s", "s-liq", "vh", null, null, null,
			SecurityConfiguration.BASIC_AUTH, false);

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
			"vh", "schema", null,
			SecurityConfiguration.BASIC_AUTH,
			replyTo.getRef(),
			mocks));

		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(new Schema.Success("schema"));
		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);
		mocks.realmProbe.expectNoMessage();

		replyTo.expectMessageClass(TenantProvisioningSaga.Success.class);

		// Assert: captured tenant has realmProvisioned = false
		Tenant tenant = captor.getValue().tenant();
		assertFalse(tenant.isRealmProvisioned());
	}

	// --- Mock Factory Helper ---

	static class MockProvisioningFactory
		implements TenantProvisioningSaga.ProvisioningFactory {

		// Creation Probes & Adapters
		final TestProbe<Realm.Command> realmProbe =
			testKit.createTestProbe();
		final AtomicReference<ActorRef<Realm.Response>> realmAdapter =
			new AtomicReference<>();

		final TestProbe<Schema.Command> schemaProbe =
			testKit.createTestProbe();
		final AtomicReference<ActorRef<Schema.Response>> schemaAdapter =
			new AtomicReference<>();

		final TestProbe<Ingress.Command> ingressProbe =
			testKit.createTestProbe();
		final AtomicReference<ActorRef<Ingress.Response>>
			ingressAdapter = new AtomicReference<>();

		// Rollback Probes & Adapters
		final TestProbe<Realm.Command> realmRollbackProbe =
			testKit.createTestProbe();
		final AtomicReference<ActorRef<Realm.Response>>
			realmRollbackAdapter = new AtomicReference<>();

		final TestProbe<Schema.Command> schemaRollbackProbe =
			testKit.createTestProbe();
		final AtomicReference<ActorRef<Schema.Response>>
			schemaRollbackAdapter = new AtomicReference<>();

		final TestProbe<Ingress.Command> ingressRollbackProbe =
			testKit.createTestProbe();
		final AtomicReference<ActorRef<Ingress.Response>>
			ingressRollbackAdapter = new AtomicReference<>();

		// --- Implementation ---

		@Override
		public Behavior<Ingress.Command> ingress(
			String s, String v,
			ActorRef<Ingress.Response> r) {

			ingressAdapter.set(r);
			return Behaviors.monitor(
				Ingress.Command.class,
				ingressProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<Ingress.Command> ingressRollback(
			String s, String v,
			ActorRef<Ingress.Response> r) {

			ingressRollbackAdapter.set(r);
			return Behaviors.monitor(
				Ingress.Command.class,
				ingressRollbackProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<Realm.Command> realm(
			String v, String s,
			ActorRef<Realm.Response> r) {

			realmAdapter.set(r);
			return Behaviors.monitor(
				Realm.Command.class,
				realmProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<Realm.Command> realmRollback(
			String s, ActorRef<Realm.Response> r) {

			realmRollbackAdapter.set(r);
			return Behaviors.monitor(
				Realm.Command.class,
				realmRollbackProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<Schema.Command> schema(
			String v, String s,
			ActorRef<Schema.Response> r) {

			schemaAdapter.set(r);
			return Behaviors.monitor(
				Schema.Command.class,
				schemaProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<Schema.Command> schemaRollback(
			String s, ActorRef<Schema.Response> r) {

			schemaRollbackAdapter.set(r);
			return Behaviors.monitor(
				Schema.Command.class,
				schemaRollbackProbe.ref(),
				Behaviors.empty());
		}
	}
}
