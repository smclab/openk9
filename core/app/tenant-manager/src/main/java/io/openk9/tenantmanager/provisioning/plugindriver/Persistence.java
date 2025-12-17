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

import io.openk9.datasource.grpc.CreatePresetPluginDriverRequest;
import io.openk9.tenantmanager.service.ConnectorProvisioningService;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

public class Persistence extends AbstractBehavior<Persistence.Command> {

	private final CreatePresetPluginDriverRequest request;

	public Persistence(
		ActorContext<Command> context,
		CreatePresetPluginDriverRequest request) {

		super(context);
		this.request = request;
	}

	public static Behavior<Command> create(
		CreatePresetPluginDriverRequest request) {

		return Behaviors.setup(ctx -> new Persistence(ctx, request));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Persist.class, this::onPersist)
			.build();
	}

	private Behavior<Command> onPersist(Persist persist) {

		var replyTo = persist.replyTo;

		getContext().pipeToSelf(
			ConnectorProvisioningService.persist(request),
			(res, err) -> new Persisted(res, err, replyTo)
		);

		return newReceiveBuilder()
			.onMessage(Persisted.class, this::onPersisted)
			.build();
	}

	private Behavior<Command> onPersisted(Persisted persisted) {
		var replyTo = persisted.replyTo();

		if (persisted.error() != null) {
			replyTo.tell(Response.ERROR);
		}
		else {
			replyTo.tell(Response.SUCCESS);
		}

		return Behaviors.stopped();
	}

	public enum Response {
		SUCCESS,
		ERROR
	}

	public sealed interface Command {}

	public record Persist(ActorRef<Response> replyTo) implements Command {}

	private record Persisted(
		Long result, Throwable error, ActorRef<Response> replyTo)
		implements Command {}

}
