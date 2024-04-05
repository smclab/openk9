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
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.grpc.CreatePluginDriverResponse;
import io.openk9.datasource.grpc.CreatePresetPluginDriverRequest;
import io.openk9.datasource.grpc.Datasource;

public class Persistence extends AbstractBehavior<Persistence.Command> {

	private final Datasource client;
	private final CreatePresetPluginDriverRequest request;
	private long pluginDriverId;

	public Persistence(
		ActorContext<Command> context,
		Datasource client,
		CreatePresetPluginDriverRequest request) {

		super(context);
		this.client = client;
		this.request = request;
	}

	public static Behavior<Command> create(
		Datasource client,
		CreatePresetPluginDriverRequest request) {

		return Behaviors.setup(ctx -> new Persistence(ctx, client, request));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Persist.class, this::onPersist)
			.build();
	}

	private Behavior<Command> onPersist(Persist persist) {

		var replyTo = persist.replyTo;

		VertxUtil.runOnContext(() -> client
			.createPresetPluginDriver(request)
			.invoke((response) -> tell(Persisted.of(response, replyTo)))
			.onFailure()
			.invoke(throwable -> replyTo.tell(Response.ERROR))
		);

		return newReceiveBuilder()
			.onMessage(Persisted.class, this::onPersisted)
			.build();
	}

	private Behavior<Command> onPersisted(Persisted persisted) {
		this.pluginDriverId = persisted.pluginDriverId();
		persisted.replyTo.tell(Response.SUCCESS);

		return Behaviors.stopped();
	}

	private void tell(Command command) {
		getContext().getSelf().tell(command);
	}

	public enum Response {
		SUCCESS,
		ERROR
	}

	public sealed interface Command {}

	public record Persist(ActorRef<Response> replyTo) implements Command {}

	private record Persisted(long pluginDriverId, ActorRef<Response> replyTo) implements Command {
		static Persisted of(CreatePluginDriverResponse response, ActorRef<Response> replyTo) {
			return new Persisted(response.getPluginDriverId(), replyTo);
		}

	}

}
