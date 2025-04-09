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

package io.openk9.tenantmanager.pipe.liquibase.validate;

import io.openk9.tenantmanager.pipe.liquibase.validate.util.Params;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;

import java.util.Deque;

public class Supervisor {

	public sealed interface Command {}
	public record Start(Deque<Params> params, ActorRef<Response> replyTo) implements Command {}
	private record MangerResponseWrapper(
		Manager.Response response, ActorRef<Response> replyTo) implements Command {}

	public sealed interface Response {}
	public enum Success implements Response {INSTANCE}
	public record Error(String message) implements Response {}

	public static Behavior<Command> create() {
		return Behaviors.setup(Supervisor::initial);
	}

	private static Behavior<Command> initial(ActorContext<Command> context) {
		return Behaviors
			.receive(Command.class)
			.onMessage(Start.class, start -> onStart(start, context))
			.onMessage(MangerResponseWrapper.class, param -> {

				Manager.Response response = param.response;

				if (response instanceof Manager.Success) {
					param.replyTo.tell(Success.INSTANCE);
				}
				else if (response instanceof Manager.Error) {
					param.replyTo.tell(new Error(((Manager.Error) response).message()));
				}

				return Behaviors.same();
			})
			.build();
	}

	private static Behavior<Command> onStart(
		Start start, ActorContext<Command> context) {

		ActorRef<Manager.Response> responseActorRef =
			context.messageAdapter(
				Manager.Response.class,
				response -> new MangerResponseWrapper(response, start.replyTo()));

		ActorRef<Manager.Command> liquibaseValidatorManagerRef =
			context.spawn(
				Manager.create(start.params(), responseActorRef),
				"liquibase-validator-" + System.currentTimeMillis());

		liquibaseValidatorManagerRef.tell(Manager.Start.INSTANCE);

		return Behaviors.same();

	}

}
