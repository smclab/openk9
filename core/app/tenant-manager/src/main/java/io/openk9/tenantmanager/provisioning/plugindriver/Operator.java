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

import io.openk9.app.manager.grpc.AppManifest;
import io.openk9.tenantmanager.service.ConnectorProvisioningService;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

public class Operator extends AbstractBehavior<Operator.Command> {

	private final AppManifest request;

	public Operator(ActorContext<Command> context, AppManifest request) {
		super(context);
		this.request = request;
	}

	public static Behavior<Command> create(AppManifest request) {

		return Behaviors.setup(ctx -> new Operator(ctx, request));
	}

	@Override
	public Receive<Command> createReceive() {

		return newReceiveBuilder()
			.onMessage(Install.class, this::onInstall)
			.build();
	}

	private Behavior<Command> onInstall(Install install) {

		var replyTo = install.replyTo;

		getContext().pipeToSelf(
			ConnectorProvisioningService.install(request),
			(res, err) -> new InstallResponse(res, err, replyTo)
		);

		return newReceiveBuilder()
			.onMessage(InstallResponse.class, this::onInstallResponse)
			.build();
	}


	private Behavior<Command> onCompensate(Compensate compensate) {

		var replyTo = compensate.replyTo;

		getContext().pipeToSelf(
			ConnectorProvisioningService.uninstall(request),
			(res, err) -> new UninstallResponse(res, err, replyTo)
		);

		return newReceiveBuilder()
			.onMessage(UninstallResponse.class, this::onUninstallResponse)
			.build();
	}

	private Behavior<Command> onInstallResponse(InstallResponse response) {
		var replyTo = response.replyTo;

		if (response.error() != null) {
			replyTo.tell(Response.ERROR);
		}
		else {
			replyTo.tell(Response.SUCCESS);
		}

		return newReceiveBuilder()
			.onMessage(Compensate.class, this::onCompensate)
			.build();
	}

	private Behavior<Command> onUninstallResponse(UninstallResponse response) {
		var replyTo = response.replyTo;

		if (response.error() != null) {
			replyTo.tell(Response.NOT_COMPENSATED);
		}
		else {
			replyTo.tell(Response.COMPENSATED);
		}

		return Behaviors.stopped();
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

	private record InstallResponse(
		Void result, Throwable error, ActorRef<Response> replyTo)
		implements Command {}

	private record UninstallResponse(
		Void result, Throwable error, ActorRef<Response> replyTo)
		implements Command {}


}
