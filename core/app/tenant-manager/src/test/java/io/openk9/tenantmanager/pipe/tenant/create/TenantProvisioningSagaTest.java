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

import java.util.concurrent.atomic.AtomicReference;

import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.service.dto.OAuth2Settings;

import io.quarkus.test.junit.QuarkusTest;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TenantProvisioningSagaTest {

	private static final ActorTestKit testKit = ActorTestKit.create();

//	@BeforeEach
//	void setup() {
//		mockedService = mockStatic(TenantProvisioningService.class);
//	}
//
//	@AfterEach
//	void tearDown() {
//		mockedService.close(); // Crucial to prevent memory leaks!
//	}

	@AfterAll
	public static void cleanup() {
		testKit.shutdownTestKit();
	}

	@Test
	void should_compensate_when_initial_step_fails() {

		TestProbe<TenantProvisioningSaga.Response> replyTo = testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vhost_1", "schema", null, replyTo.getRef(), mocks));

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
		mocks.schemaRollbackProbe.expectMessage(Schema.Rollback.INSTANCE);
		// CRITICAL: We must reply to the rollback adapter to tell Saga it's done
		mocks.schemaRollbackAdapter.get().tell(new Schema.Success("Rolled back"));

		// --- Fix: Handle Ingress Rollback ---
		mocks.ingressRollbackProbe.expectMessage(Ingress.Start.INSTANCE);
		// CRITICAL: Reply to adapter
		mocks.ingressRollbackAdapter.get().tell(Ingress.Success.INSTANCE);

		// 3. Verify Final Saga Error
		// Now that compensations are confirmed, the Saga should finally reply
		replyTo.expectMessage(TenantProvisioningSaga.Error.INSTANCE);
	}

	@Test
	void should_succeed_when_all_children_succeed() {

		TestProbe<TenantProvisioningSaga.Response> replyTo = testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vhost_1", "tenant_1", null, replyTo.getRef(), mocks));

		// 1. Verify Parallel Starts and Reply Success
		mocks.realmProbe.expectMessage(Realm.Start.INSTANCE);
		mocks.realmAdapter.get().tell(new Realm.Success("cid", "csec", "vhost", "iss", "usr", "pwd"));

		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(new Schema.Success("tenant_1"));

		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);

		replyTo.expectMessageClass(TenantProvisioningSaga.Success.class);
	}

	@Test
	void should_succeed_with_oauth2_settings() {

		TenantResponseDTO expectedTenant = new TenantResponseDTO(
			"1213402942",
			"tenant_2",
			"tenant_2_liquibase",
			"vh",
			"cid",
			"csec",
			"issuer"
		);

		TestProbe<TenantProvisioningSaga.Response> replyTo = testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();
		OAuth2Settings settings = new OAuth2Settings("cid", "csec", "issuer");

		testKit.spawn(TenantProvisioningSaga.create(
			"vh", "tenant_2", settings, replyTo.getRef(), mocks));

		// 1. Verify Parallel Starts (Realm skipped due to OAuth2Settings)
		// We expect schema and ingress to start
		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(new Schema.Success("tenant_2"));

		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);

		// Realm should NOT start
		mocks.realmProbe.expectNoMessage();

		// 2. Verify Final Saga Response
		replyTo.expectMessageClass(TenantProvisioningSaga.Success.class);
	}

	@Test
	void should_generate_name_when_null() {

		TestProbe<TenantProvisioningSaga.Response> replyTo = testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vh_2", null, null, replyTo.getRef(), mocks));

		// 1. Verify Parallel Starts (Realm skipped due to OAuth2Settings)
		mocks.realmProbe.expectMessage(Realm.Start.INSTANCE);
		mocks.realmAdapter.get().tell(
			new Realm.Success("cid", "csec", "vh", "iss", "usr", "pwd"));

		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(new Schema.Success("generated-schema"));

		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);

		// 3. Verify Final Saga Response
		TenantProvisioningSaga.Success success = replyTo.expectMessageClass(TenantProvisioningSaga.Success.class);

		// Verify name was used
		// The ID and Name come from the expectedTenant we mocked
		var tenant = success.tenant();
		Assertions.assertEquals("generated-schema", tenant.schemaName());
	}

	// --- Mock Factory Helper ---

	static class MockProvisioningFactory implements TenantProvisioningSaga.ProvisioningFactory {

		// Creation Probes & Adapters
		final TestProbe<Realm.Command> realmProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<Realm.Response>> realmAdapter = new AtomicReference<>();

		final TestProbe<Schema.Command> schemaProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<Schema.Response>> schemaAdapter = new AtomicReference<>();

		final TestProbe<Ingress.Command> ingressProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<Ingress.Response>> ingressAdapter = new AtomicReference<>();

		// Rollback Probes & Adapters
		final TestProbe<Realm.Command> realmRollbackProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<Realm.Response>> realmRollbackAdapter = new AtomicReference<>();

		final TestProbe<Schema.Command> schemaRollbackProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<Schema.Response>> schemaRollbackAdapter = new AtomicReference<>();

		final TestProbe<Ingress.Command> ingressRollbackProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<Ingress.Response>> ingressRollbackAdapter = new AtomicReference<>();

		// --- Implementation ---

		@Override
		public Behavior<Ingress.Command> ingress(String s, String v, ActorRef<Ingress.Response> r) {

			ingressAdapter.set(r);
			return Behaviors.monitor(Ingress.Command.class, ingressProbe.ref(), Behaviors.empty());
		}

		@Override
		public Behavior<Ingress.Command> ingressRollback(
			String s, String v, ActorRef<Ingress.Response> r) {

			ingressRollbackAdapter.set(r);

			return Behaviors.monitor(
				Ingress.Command.class,
				ingressRollbackProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<Realm.Command> realm(String v, String s, ActorRef<Realm.Response> r) {

			realmAdapter.set(r);

			return Behaviors.monitor(
				Realm.Command.class, realmProbe.ref(), Behaviors.empty());
		}

		@Override
		public Behavior<Realm.Command> realmRollback(String s, ActorRef<Realm.Response> r) {

			realmRollbackAdapter.set(r);

			return Behaviors.monitor(
				Realm.Command.class,
				realmRollbackProbe.ref(),
				Behaviors.empty());
		}

		@Override
		public Behavior<Schema.Command> schema(String v, String s, ActorRef<Schema.Response> r) {

			schemaAdapter.set(r);

			return Behaviors.monitor(
				Schema.Command.class, schemaProbe.ref(), Behaviors.empty());
		}

		@Override
		public Behavior<Schema.Command> schemaRollback(String s, ActorRef<Schema.Response> r) {

			schemaRollbackAdapter.set(r);

			return Behaviors.monitor(
				Schema.Command.class,
				schemaRollbackProbe.ref(),
				Behaviors.empty());
		}

	}
}
