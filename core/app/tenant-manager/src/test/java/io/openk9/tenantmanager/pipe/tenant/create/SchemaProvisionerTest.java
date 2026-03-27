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

import java.time.Duration;

import io.openk9.quarkus.common.EventBusInstanceHolder;
import io.openk9.tenantmanager.service.TenantProvisioningService;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SchemaProvisioner} provisioning and rollback
 * behaviors. Verifies that the actor replies to the saga before
 * stopping in all code paths.
 */
@DisplayName("SchemaProvisioner")
class SchemaProvisionerTest {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private static final ActorTestKit testKit =
		ActorTestKit.create();

	private EventBus eventBus;

	@AfterAll
	static void cleanup() {
		testKit.shutdownTestKit();
	}

	@BeforeEach
	void setUp() {
		eventBus = mock(EventBus.class);
		EventBusInstanceHolder.setEventBus(eventBus);
	}

	@Nested
	@DisplayName("create (provisioning)")
	class Create {

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName(
			"replies with Success containing tenant name")
		void repliesOnSuccess() {
			// mock EventBus so createSchema succeeds
			Message<Void> mockMsg = mock(Message.class);
			when(mockMsg.body()).thenReturn(null);
			when(eventBus.<Void>request(
				eq(TenantProvisioningService.CREATE_SCHEMA),
				any()))
				.thenReturn(Uni.createFrom().item(mockMsg));

			TestProbe<SchemaProvisioner.Response> replyTo =
				testKit.createTestProbe();

			// spawn and send Start
			ActorRef<SchemaProvisioner.Command> actor =
				testKit.spawn(SchemaProvisioner.create(
					"vhost", "mytenant", replyTo.getRef()));
			actor.tell(SchemaProvisioner.Start.INSTANCE);

			var response = replyTo.expectMessageClass(
				SchemaProvisioner.Success.class, TIMEOUT);

			assertEquals("mytenant", response.tenantName());
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName("replies with Error when creation fails")
		void repliesOnFailure() {
			// mock EventBus so createSchema fails
			when(eventBus.<Void>request(
				eq(TenantProvisioningService.CREATE_SCHEMA),
				any()))
				.thenReturn(Uni.createFrom().failure(
					new RuntimeException(
						"CREATE SCHEMA failed")));

			TestProbe<SchemaProvisioner.Response> replyTo =
				testKit.createTestProbe();

			ActorRef<SchemaProvisioner.Command> actor =
				testKit.spawn(SchemaProvisioner.create(
					"vhost", "mytenant", replyTo.getRef()));
			actor.tell(SchemaProvisioner.Start.INSTANCE);

			var response = replyTo.expectMessageClass(
				SchemaProvisioner.Error.class, TIMEOUT);

			assertTrue(response.message()
				.contains("CREATE SCHEMA failed"));
		}
	}

	@Nested
	@DisplayName("rollback")
	class RollbackTests {

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName(
			"replies with Success after schema deletion")
		void repliesOnSuccess() {
			// mock EventBus so deleteSchema succeeds
			Message<Void> mockMsg = mock(Message.class);
			when(mockMsg.body()).thenReturn(null);
			when(eventBus.<Void>request(
				eq(TenantProvisioningService.DELETE_SCHEMA),
				any()))
				.thenReturn(Uni.createFrom().item(mockMsg));

			TestProbe<SchemaProvisioner.Response> replyTo =
				testKit.createTestProbe();

			ActorRef<SchemaProvisioner.Command> actor =
				testKit.spawn(SchemaProvisioner.createRollback(
					"mytenant", replyTo.getRef()));
			actor.tell(SchemaProvisioner.Rollback.INSTANCE);

			var response = replyTo.expectMessageClass(
				SchemaProvisioner.Success.class, TIMEOUT);

			assertEquals("mytenant", response.tenantName());
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName(
			"replies with Error when schema deletion fails")
		void repliesOnFailure() {
			// mock EventBus so deleteSchema fails
			when(eventBus.<Void>request(
				eq(TenantProvisioningService.DELETE_SCHEMA),
				any()))
				.thenReturn(Uni.createFrom().failure(
					new RuntimeException(
						"DROP SCHEMA failed")));

			TestProbe<SchemaProvisioner.Response> replyTo =
				testKit.createTestProbe();

			ActorRef<SchemaProvisioner.Command> actor =
				testKit.spawn(SchemaProvisioner.createRollback(
					"mytenant", replyTo.getRef()));
			actor.tell(SchemaProvisioner.Rollback.INSTANCE);

			var response = replyTo.expectMessageClass(
				SchemaProvisioner.Error.class, TIMEOUT);

			assertTrue(response.message()
				.contains("DROP SCHEMA failed"));
		}
	}

}
