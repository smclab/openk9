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
import io.openk9.tenantmanager.service.TenantRealmService;

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
 * Tests for {@link RealmProvisioner} provisioning and rollback
 * behaviors. Verifies that the actor replies to the saga before
 * stopping in all code paths.
 */
@DisplayName("RealmProvisioner")
class RealmProvisionerTest {

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
			"replies with Success containing realm details")
		void repliesOnSuccess() {
			// mock EventBus so createRealm succeeds
			var createdRealm = new TenantRealmService.CreatedRealm(
				"cid", "csec", "vhost",
				"https://idp/realms/test", "admin", "pwd");

			Message<TenantRealmService.CreatedRealm> mockMsg =
				mock(Message.class);
			when(mockMsg.body()).thenReturn(createdRealm);
			when(eventBus
				.<TenantRealmService.CreatedRealm>request(
					eq(TenantProvisioningService.CREATE_REALM),
					any()))
				.thenReturn(Uni.createFrom().item(mockMsg));

			TestProbe<RealmProvisioner.Response> replyTo =
				testKit.createTestProbe();

			// spawn and send Start
			ActorRef<RealmProvisioner.Command> actor =
				testKit.spawn(RealmProvisioner.create(
					"vhost", "test", replyTo.getRef()));
			actor.tell(RealmProvisioner.Start.INSTANCE);

			// verify Success with correct fields
			var response = replyTo.expectMessageClass(
				RealmProvisioner.Success.class, TIMEOUT);

			assertEquals("cid", response.clientId());
			assertEquals("csec", response.clientSecret());
			assertEquals(
				"https://idp/realms/test",
				response.issuerUri());
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName("replies with Error when creation fails")
		void repliesOnFailure() {
			// mock EventBus so createRealm fails
			when(eventBus
				.<TenantRealmService.CreatedRealm>request(
					eq(TenantProvisioningService.CREATE_REALM),
					any()))
				.thenReturn(Uni.createFrom().failure(
					new RuntimeException("Keycloak down")));

			TestProbe<RealmProvisioner.Response> replyTo =
				testKit.createTestProbe();

			ActorRef<RealmProvisioner.Command> actor =
				testKit.spawn(RealmProvisioner.create(
					"vhost", "test", replyTo.getRef()));
			actor.tell(RealmProvisioner.Start.INSTANCE);

			var response = replyTo.expectMessageClass(
				RealmProvisioner.Error.class, TIMEOUT);

			assertTrue(
				response.message().contains("Keycloak down"));
		}
	}

	@Nested
	@DisplayName("rollback")
	class RollbackTests {

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName(
			"replies with SuccessRollback after realm deletion")
		void repliesOnSuccess() {
			// mock EventBus so deleteRealm succeeds
			Message<Void> mockMsg = mock(Message.class);
			when(mockMsg.body()).thenReturn(null);
			when(eventBus.<Void>request(
				eq(TenantProvisioningService.DELETE_REALM),
				any()))
				.thenReturn(Uni.createFrom().item(mockMsg));

			TestProbe<RealmProvisioner.Response> replyTo =
				testKit.createTestProbe();

			ActorRef<RealmProvisioner.Command> actor =
				testKit.spawn(RealmProvisioner.createRollback(
					"test", replyTo.getRef()));
			actor.tell(RealmProvisioner.Rollback.INSTANCE);

			replyTo.expectMessageClass(
				RealmProvisioner.SuccessRollback.class,
				TIMEOUT);
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName(
			"replies with Error when realm deletion fails")
		void repliesOnFailure() {
			// mock EventBus so deleteRealm fails
			when(eventBus.<Void>request(
				eq(TenantProvisioningService.DELETE_REALM),
				any()))
				.thenReturn(Uni.createFrom().failure(
					new RuntimeException("Realm not found")));

			TestProbe<RealmProvisioner.Response> replyTo =
				testKit.createTestProbe();

			ActorRef<RealmProvisioner.Command> actor =
				testKit.spawn(RealmProvisioner.createRollback(
					"test", replyTo.getRef()));
			actor.tell(RealmProvisioner.Rollback.INSTANCE);

			var response = replyTo.expectMessageClass(
				RealmProvisioner.Error.class, TIMEOUT);

			assertTrue(
				response.message().contains("Realm not found"));
		}
	}

}
