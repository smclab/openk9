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

import java.util.Optional;

import io.openk9.app.manager.grpc.CreateIngressResponse;
import io.openk9.app.manager.grpc.DeleteIngressResponse;
import io.openk9.tenantmanager.service.TenantProvisioningService;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.eclipse.microprofile.config.ConfigProvider;

public class Ingress extends AbstractBehavior<Ingress.Command> {

	private final String schemaName;
	private final String virtualHost;
	private final boolean defaultBehavior;
	private final ActorRef<Ingress.Response> replyTo;

	public Ingress(
		ActorContext<Command> context,
		String schemaName,
		String virtualHost,
		boolean defaultBehavior,
		ActorRef<Ingress.Response> replyTo
	) {

		super(context);
		this.schemaName = schemaName;
		this.virtualHost = virtualHost;
		this.defaultBehavior = defaultBehavior;
		this.replyTo = replyTo;
	}

	public static Behavior<Command> create(
		String schemaName,
		String virtualHost,
		ActorRef<Ingress.Response> replyTo) {

		return Behaviors.setup(ctx ->
			new Ingress(ctx, schemaName, virtualHost, true, replyTo));
	}

	public static Behavior<Command> rollback(
		String schemaName, String virtualHost, ActorRef<Response> replyTo) {

		return Behaviors.setup(ctx ->
			new Ingress(ctx, schemaName, virtualHost, false, replyTo));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Start.class, this::isDefaultBehavior, this::onStartDefault)
			.onMessage(Start.class, this::isRollbackBehavior, this::onStartRollback)
			.build();
	}

	private static Optional<String> k8sNamespace() {
		ConfigProvider.getConfig().getConfigSources();

		return ConfigProvider
			.getConfig()
			.getOptionalValue(
				"quarkus.kubernetes.namespace",
				String.class
			);
	}

	private Behavior<Command> onHandleRollback(HandleRollback handleRollback) {
		return Behaviors.stopped();
	}

	private boolean isDefaultBehavior(Command command) {
		return this.defaultBehavior;
	}

	private boolean isRollbackBehavior(Command command) {
		return !isDefaultBehavior(command);
	}

	private Behavior<Command> onHandleCreate(HandleCreate handleCreate) {
		var throwable = handleCreate.throwable;

		if (throwable == null) {
			replyTo.tell(Success.INSTANCE);
		}
		else {
			replyTo.tell(new Error(new IngressException(throwable)));
		}

		return Behaviors.stopped();
	}

	private Behavior<Command> onStartRollback(Command ignore) {

		if (k8sNamespace().isPresent()) {

			getContext().pipeToSelf(
				TenantProvisioningService.deleteIngress(virtualHost, schemaName),
				(result, throwable) -> {
					IngressException exception = null;
					if (throwable != null) {
						exception = new IngressException(throwable);
					}

					return new HandleRollback(result, exception);
				}
			);
		}
		else {

			getContext().getLog().info("Skipped. Kubernetes namespace not defined.");
			replyTo.tell(Success.INSTANCE);
		}

		return newReceiveBuilder()
			.onMessage(HandleRollback.class, this::onHandleRollback)
			.build();
	}

	private Behavior<Command> onStartDefault(Command ignore) {

		if (k8sNamespace().isPresent()) {
			getContext().pipeToSelf(
				TenantProvisioningService.createIngress(virtualHost, schemaName),
				HandleCreate::new
			);
		}
		else {

			getContext().getLog().info(
				"Skipping ingress creation. No kubernetes namespace defined.");
			replyTo.tell(Success.INSTANCE);
		}

		return newReceiveBuilder()
			.onMessage(HandleCreate.class, this::onHandleCreate)
			.build();
	}

	public sealed interface Response {}
	public sealed interface Command {}

	public enum Start implements Command {INSTANCE}
	private record HandleCreate(
		CreateIngressResponse response,
		Throwable throwable) implements Command {}

	public enum Success implements Response {INSTANCE}

	public record Error(IngressException exception) implements Response {}

	private record HandleRollback(
		DeleteIngressResponse response,
		IngressException exception
	) implements Command {}

}
