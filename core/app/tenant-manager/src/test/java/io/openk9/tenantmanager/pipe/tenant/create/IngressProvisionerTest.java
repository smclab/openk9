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

import io.openk9.app.manager.grpc.CreateIngressResponse;
import io.openk9.app.manager.grpc.DeleteIngressResponse;
import io.openk9.quarkus.common.EventBusInstanceHolder;
import io.openk9.tenantmanager.service.TenantProvisioningService;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.apache.pekko.actor.testkit.typed.javadsl.ActorTestKit;
import org.apache.pekko.actor.testkit.typed.javadsl.TestProbe;
import org.apache.pekko.actor.typed.ActorRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link IngressProvisioner} provisioning and rollback
 * behaviors. Verifies that the actor replies to the saga before
 * stopping in all code paths.
 */
@DisplayName("IngressProvisioner")
class IngressProvisionerTest {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private static final ActorTestKit testKit =
		ActorTestKit.create();

	@AfterAll
	static void cleanup() {
		testKit.shutdownTestKit();
	}

	@BeforeEach
	void setUp() {
		System.setProperty(
			"quarkus.kubernetes.namespace", "test-ns");
	}

	@AfterEach
	void tearDown() {
		System.clearProperty("quarkus.kubernetes.namespace");
	}

	@Nested
	@DisplayName("create (provisioning)")
	class Create {

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName("replies with Success after ingress creation")
		void repliesOnSuccess() {
			// mock EventBus so createIngress succeeds
			EventBus eventBus = mock(EventBus.class);
			EventBusInstanceHolder.setEventBus(eventBus);

			Message<CreateIngressResponse> mockMsg =
				mock(Message.class);
			when(mockMsg.body()).thenReturn(
				CreateIngressResponse.getDefaultInstance());
			when(eventBus.<CreateIngressResponse>request(
				eq(TenantProvisioningService.CREATE_INGRESS),
				any()))
				.thenReturn(Uni.createFrom().item(mockMsg));

			TestProbe<IngressProvisioner.Response> replyTo =
				testKit.createTestProbe();

			// spawn and send Start
			ActorRef<IngressProvisioner.Command> actor =
				testKit.spawn(IngressProvisioner.create(
					"vhost", "tenant", replyTo.getRef()));
			actor.tell(IngressProvisioner.Start.INSTANCE);

			replyTo.expectMessage(
				TIMEOUT, IngressProvisioner.Success.INSTANCE);
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName("replies with Error when creation fails")
		void repliesOnFailure() {
			// mock EventBus so createIngress fails
			EventBus eventBus = mock(EventBus.class);
			EventBusInstanceHolder.setEventBus(eventBus);

			when(eventBus.<CreateIngressResponse>request(
				eq(TenantProvisioningService.CREATE_INGRESS),
				any()))
				.thenReturn(Uni.createFrom().failure(
					new RuntimeException("gRPC unavailable")));

			TestProbe<IngressProvisioner.Response> replyTo =
				testKit.createTestProbe();

			ActorRef<IngressProvisioner.Command> actor =
				testKit.spawn(IngressProvisioner.create(
					"vhost", "tenant", replyTo.getRef()));
			actor.tell(IngressProvisioner.Start.INSTANCE);

			replyTo.expectMessageClass(
				IngressProvisioner.Error.class, TIMEOUT);
		}

		@Test
		@DisplayName(
			"skips and replies Success when no k8s namespace")
		void skipsWhenNoNamespace() {
			System.clearProperty("quarkus.kubernetes.namespace");

			TestProbe<IngressProvisioner.Response> replyTo =
				testKit.createTestProbe();

			ActorRef<IngressProvisioner.Command> actor =
				testKit.spawn(IngressProvisioner.create(
					"vhost", "tenant", replyTo.getRef()));
			actor.tell(IngressProvisioner.Start.INSTANCE);

			replyTo.expectMessage(
				TIMEOUT, IngressProvisioner.Success.INSTANCE);
		}
	}

	@Nested
	@DisplayName("rollback")
	class Rollback {

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName(
			"replies with Success after ingress deletion")
		void repliesOnSuccess() {
			// mock EventBus so deleteIngress succeeds
			EventBus eventBus = mock(EventBus.class);
			EventBusInstanceHolder.setEventBus(eventBus);

			Message<DeleteIngressResponse> mockMsg =
				mock(Message.class);
			when(mockMsg.body()).thenReturn(
				DeleteIngressResponse.getDefaultInstance());
			when(eventBus.<DeleteIngressResponse>request(
				eq(TenantProvisioningService.DELETE_INGRESS),
				any()))
				.thenReturn(Uni.createFrom().item(mockMsg));

			TestProbe<IngressProvisioner.Response> replyTo =
				testKit.createTestProbe();

			ActorRef<IngressProvisioner.Command> actor =
				testKit.spawn(IngressProvisioner.rollback(
					"vhost", "tenant", replyTo.getRef()));
			actor.tell(IngressProvisioner.Start.INSTANCE);

			replyTo.expectMessage(
				TIMEOUT, IngressProvisioner.Success.INSTANCE);
		}

		@Test
		@SuppressWarnings("unchecked")
		@DisplayName(
			"replies with Error when deletion fails")
		void repliesOnFailure() {
			// mock EventBus so deleteIngress fails
			EventBus eventBus = mock(EventBus.class);
			EventBusInstanceHolder.setEventBus(eventBus);

			when(eventBus.<DeleteIngressResponse>request(
				eq(TenantProvisioningService.DELETE_INGRESS),
				any()))
				.thenReturn(Uni.createFrom().failure(
					new RuntimeException("gRPC unavailable")));

			TestProbe<IngressProvisioner.Response> replyTo =
				testKit.createTestProbe();

			ActorRef<IngressProvisioner.Command> actor =
				testKit.spawn(IngressProvisioner.rollback(
					"vhost", "tenant", replyTo.getRef()));
			actor.tell(IngressProvisioner.Start.INSTANCE);

			replyTo.expectMessageClass(
				IngressProvisioner.Error.class, TIMEOUT);
		}

		@Test
		@DisplayName(
			"skips and replies Success when no k8s namespace")
		void skipsWhenNoNamespace() {
			System.clearProperty("quarkus.kubernetes.namespace");

			TestProbe<IngressProvisioner.Response> replyTo =
				testKit.createTestProbe();

			ActorRef<IngressProvisioner.Command> actor =
				testKit.spawn(IngressProvisioner.rollback(
					"vhost", "tenant", replyTo.getRef()));
			actor.tell(IngressProvisioner.Start.INSTANCE);

			replyTo.expectMessage(
				TIMEOUT, IngressProvisioner.Success.INSTANCE);
		}
	}

}
