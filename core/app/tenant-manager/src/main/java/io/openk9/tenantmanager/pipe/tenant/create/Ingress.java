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

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.CreateIngressRequest;
import io.openk9.app.manager.grpc.CreateIngressResponse;
import io.openk9.app.manager.grpc.DeleteIngressRequest;
import io.openk9.app.manager.grpc.DeleteIngressResponse;
import io.openk9.common.util.VertxUtil;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Optional;

public class Ingress extends AbstractBehavior<Ingress.Command> {

	private final AppManager appManager;
	private final String schemaName;
	private final String virtualHost;
	private final boolean defaultBehavior;
	private final ActorRef<Ingress.Response> replyTo;

	public Ingress(
		ActorContext<Command> context,
		AppManager appManager,
		String schemaName,
		String virtualHost,
		boolean defaultBehavior,
		ActorRef<Ingress.Response> replyTo
	) {

		super(context);
		this.appManager = appManager;
		this.schemaName = schemaName;
		this.virtualHost = virtualHost;
		this.defaultBehavior = defaultBehavior;
		this.replyTo = replyTo;
	}

	public static Behavior<Command> create(
		AppManager appManager, String schemaName, String virtualHost,
		ActorRef<Ingress.Response> replyTo) {

		return Behaviors.setup(ctx ->
			new Ingress(ctx, appManager, schemaName, virtualHost, true, replyTo));
	}

	public static Behavior<Command> rollback(
		AppManager appManager, String schemaName, String virtualHost) {

		return Behaviors.setup(ctx ->
			new Ingress(ctx, appManager, schemaName, virtualHost, false, null));
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
			replyTo.tell(new Error(throwable));
		}

		return Behaviors.stopped();
	}

	private Behavior<Command> onStartRollback(Command ignore) {

		k8sNamespace()
			.ifPresentOrElse(
				(ns) -> VertxUtil.runOnContext(() -> appManager
					.deleteIngress(DeleteIngressRequest.newBuilder()
						.setSchemaName(schemaName)
						.setVirtualHost(virtualHost)
						.build())
					.onItemOrFailure()
					.invoke((response, throwable) -> getContext()
						.getSelf()
						.tell(new HandleRollback(response, throwable))
					)
				),
				() -> {
					getContext().getLog().info("Skipped. Kubernetes namespace not defined.");
					replyTo.tell(Success.INSTANCE);
				}
			);


		return newReceiveBuilder()
			.onMessage(HandleRollback.class, this::onHandleRollback)
			.build();
	}

	private Behavior<Command> onStartDefault(Command ignore) {

		k8sNamespace()
			.ifPresentOrElse(
				(ns) -> VertxUtil.runOnContext(() ->
					appManager.createIngress(CreateIngressRequest
							.newBuilder()
							.setSchemaName(schemaName)
							.setVirtualHost(virtualHost)
							.build())
						.onItemOrFailure()
						.invoke((response, throwable) -> getContext()
							.getSelf()
							.tell(new HandleCreate(response, throwable))
						)
				),
				() -> {
					getContext().getLog().info(
						"Skipping ingress creation. No kubernetes namespace defined.");
					replyTo.tell(Success.INSTANCE);
				}
			);

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
	public record Error(Throwable cause) implements Response {}

	private record HandleRollback(
		DeleteIngressResponse response,
		Throwable throwable) implements Command {}

}
