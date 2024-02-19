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

package io.openk9.tenantmanager.provisioning.plugindriver;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.app.manager.grpc.AppManager;
import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.common.util.VertxUtil;

public class Operator extends AbstractBehavior<Operator.Command> {

	private final AppManager client;
	private final AppManifest request;

	public Operator(
		ActorContext<Command> context,
		AppManager client,
		AppManifest request) {

		super(context);
		this.client = client;
		this.request = request;

	}

	public static Behavior<Command> create(
		AppManager client,
		AppManifest request) {

		return Behaviors.setup(ctx -> new Operator(ctx, client, request));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Install.class, this::onInstall)
			.build();
	}

	private Behavior<Command> onInstall(Install install) {

		var replyTo = install.replyTo;

		VertxUtil.runOnContext(() -> client
			.applyResource(request)
			.invoke(() -> tell(new Installed(replyTo)))
			.onFailure()
			.invoke(() -> replyTo.tell(Response.ERROR))
		);

		return newReceiveBuilder()
			.onMessage(Installed.class, this::onInstalled)
			.build();
	}

	private Behavior<Command> onCompensate(Compensate compensate) {

		var replyTo = compensate.replyTo;

		VertxUtil.runOnContext(() -> client
			.deleteResource(request)
			.invoke(() -> tell(new Compensated(replyTo)))
			.onFailure()
			.invoke(() -> replyTo.tell(Response.NOT_COMPENSATED))
		);

		return newReceiveBuilder()
			.onMessage(Compensated.class, this::onCompensated)
			.build();
	}


	private Behavior<Command> onInstalled(Installed installed) {
		var replyTo = installed.replyTo;

		replyTo.tell(Response.SUCCESS);

		return newReceiveBuilder()
			.onMessage(Compensate.class, this::onCompensate)
			.build();
	}

	private Behavior<Command> onCompensated(Compensated compensated) {
		var replyTo = compensated.replyTo;

		replyTo.tell(Response.COMPENSATED);

		return Behaviors.stopped();
	}

	private void tell(Command command) {
		getContext().getSelf().tell(command);
	}

	public enum Response {
		SUCCESS,
		ERROR,
		COMPENSATED,
		NOT_COMPENSATED
	}

	public sealed interface Command {}

	public record Install(ActorRef<Response> replyTo) implements Command {}

	public record Compensate(ActorRef<Response> replyTo) implements Command {}

	private record Compensated(ActorRef<Response> replyTo) implements Command {}

	private record Installed(ActorRef<Response> replyTo) implements Command {}

}
