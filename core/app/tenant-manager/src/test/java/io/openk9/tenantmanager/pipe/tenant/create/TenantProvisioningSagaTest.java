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

import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class TenantProvisioningSagaTest {

	private static final ActorTestKit testKit = ActorTestKit.create();

	@AfterAll
	public static void cleanup() {
		testKit.shutdownTestKit();
	}

	@Test
	void should_succeed_when_all_children_succeed() {
		TestProbe<TenantProvisioningSaga.Response> replyTo = testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vhost", "schema", replyTo.getRef(), mocks
		));

		// 1. Verify Parallel Starts and Reply Success
		mocks.realmProbe.expectMessage(Realm.Start.INSTANCE);
		mocks.realmAdapter.get().tell(new Realm.Success("cid", "sec", "vhost", "iss", "usr", "pwd"));

		mocks.schemaProbe.expectMessage(Schema.Start.INSTANCE);
		mocks.schemaAdapter.get().tell(new Schema.Success("schema"));

		mocks.ingressProbe.expectMessage(Ingress.Start.INSTANCE);
		mocks.ingressAdapter.get().tell(Ingress.Success.INSTANCE);

		// 2. Verify Tenant Start (triggered after all 3 above succeed)
		mocks.tenantProbe.expectMessage(Tenant.Start.INSTANCE);

		// Reply with Tenant Success
		mocks.tenantAdapter.get().tell(new Tenant.Success(new TenantResponseDTO(
			"1213402949",
			"mySchema",
			"mySchema_liquibase",
			"vHost",
			"cid",
			"sec",
			"issuerUri"
		)));

		// 3. Verify Final Saga Response
		replyTo.expectMessageClass(TenantProvisioningSaga.Success.class);
	}

	@Test
	void should_compensate_when_initial_step_fails() {
		TestProbe<TenantProvisioningSaga.Response> replyTo = testKit.createTestProbe();
		MockProvisioningFactory mocks = new MockProvisioningFactory();

		testKit.spawn(TenantProvisioningSaga.create(
			"vhost", "schema", replyTo.getRef(), mocks
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
		mocks.schemaRollbackProbe.expectMessage(Schema.Rollback.INSTANCE);
		// CRITICAL: We must reply to the rollback adapter to tell Saga it's done
		mocks.schemaRollbackAdapter.get().tell(new Schema.Success("Rolled back"));

		// --- Fix: Handle Ingress Rollback ---
		// Note: Check your Saga code to see if it sends Start or Rollback to Ingress
		mocks.ingressRollbackProbe.expectMessage(Ingress.Start.INSTANCE);
		// CRITICAL: Reply to adapter
		mocks.ingressRollbackAdapter.get().tell(Ingress.Success.INSTANCE);

		// 3. Verify Tenant was NEVER created
		mocks.tenantProbe.expectNoMessage();

		// 4. Verify Final Saga Error
		// Now that compensations are confirmed, the Saga should finally reply
		replyTo.expectMessage(TenantProvisioningSaga.Error.INSTANCE);
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

		final TestProbe<Tenant.Command> tenantProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<Tenant.Response>> tenantAdapter = new AtomicReference<>();

		// Rollback Probes & Adapters
		final TestProbe<Realm.Command> realmRollbackProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<Realm.Response>> realmRollbackAdapter = new AtomicReference<>();

		final TestProbe<Schema.Command> schemaRollbackProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<Schema.Response>> schemaRollbackAdapter = new AtomicReference<>();

		final TestProbe<Ingress.Command> ingressRollbackProbe = testKit.createTestProbe();
		final AtomicReference<ActorRef<Ingress.Response>> ingressRollbackAdapter = new AtomicReference<>();

		// --- Implementation ---

		@Override
		public Behavior<Realm.Command> realm(String v, String s, ActorRef<Realm.Response> r) {
			realmAdapter.set(r);
			return Behaviors.monitor(Realm.Command.class, realmProbe.ref(), Behaviors.empty());
		}

		@Override
		public Behavior<Schema.Command> schema(String v, String s, ActorRef<Schema.Response> r) {
			schemaAdapter.set(r);
			return Behaviors.monitor(Schema.Command.class, schemaProbe.ref(), Behaviors.empty());
		}

		@Override
		public Behavior<Ingress.Command> ingress(String s, String v, ActorRef<Ingress.Response> r) {
			ingressAdapter.set(r);
			return Behaviors.monitor(Ingress.Command.class, ingressProbe.ref(), Behaviors.empty());
		}

		@Override
		public Behavior<Tenant.Command> tenant(
			String v, String s, String l, String i, String c, String sc,
			ActorRef<Tenant.Response> r) {

			tenantAdapter.set(r);
			return Behaviors.monitor(Tenant.Command.class, tenantProbe.ref(), Behaviors.empty());
		}

		@Override
		public Behavior<Realm.Command> realmRollback(String s, ActorRef<Realm.Response> r) {
			realmRollbackAdapter.set(r);
			return Behaviors.monitor(Realm.Command.class, realmRollbackProbe.ref(), Behaviors.empty());
		}

		@Override
		public Behavior<Schema.Command> schemaRollback(String s, ActorRef<Schema.Response> r) {
			schemaRollbackAdapter.set(r);
			return Behaviors.monitor(
				Schema.Command.class, schemaRollbackProbe.ref(), Behaviors.empty());
		}

		@Override
		public Behavior<Ingress.Command> ingressRollback(String s, String v, ActorRef<Ingress.Response> r) {
			ingressRollbackAdapter.set(r);
			return Behaviors.monitor(
				Ingress.Command.class, ingressRollbackProbe.ref(), Behaviors.empty());
		}
	}
}
