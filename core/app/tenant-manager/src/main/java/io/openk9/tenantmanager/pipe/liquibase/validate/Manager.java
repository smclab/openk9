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

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.tenantmanager.pipe.liquibase.validate.util.Params;
import io.quarkus.runtime.util.ExceptionUtil;

import java.util.Deque;

public class Manager {

	public sealed interface Command {}
	public enum Start implements Command { INSTANCE }
	private record LiquibaseResponseWrapper(
		Liquibase.Response response) implements Command {}
	private enum Timeout implements Command { INSTANCE }

	public sealed interface Response {}
	public enum Success implements Response {INSTANCE}
	public record Error(String message) implements Response {}

	public static Behavior<Command> create(
		Deque<Params> liquibaseParamsList, ActorRef<Response> replyTo) {
		return Behaviors.setup(context -> initial(liquibaseParamsList, replyTo, context));
	}

	private static Behavior<Command> initial(
		Deque<Params> liquibaseParamsList, ActorRef<Response> replyTo,
		ActorContext<Command> context) {

		int size = liquibaseParamsList.size();

		return Behaviors
			.receive(Command.class)
			.onMessage(Start.class, start -> onStart(liquibaseParamsList, replyTo, context))
			.onMessageEquals(Timeout.INSTANCE, () -> onTimeout(replyTo))
			.build();
	}

	private static Behavior<Command> onTimeout(ActorRef<Response> replyTo) {

		replyTo.tell(new Error("Timeout"));

		return Behaviors.stopped();

	}

	private static Behavior<Command> onStart(
		Deque<Params> liquibaseParamsList, ActorRef<Response> replyTo,
		ActorContext<Command> context) {

		if (liquibaseParamsList.isEmpty()) {
			replyTo.tell(Success.INSTANCE);
			return Behaviors.stopped();
		}

		Params firstLiquibaseParams = liquibaseParamsList.pop();

		ActorRef<Liquibase.Response> responseActorRef =
			context.messageAdapter(Liquibase.Response.class,
				LiquibaseResponseWrapper::new);

		ActorRef<Liquibase.Command> liquibaseValidateRef = context.spawn(
			Liquibase.validate(firstLiquibaseParams, responseActorRef),
			"liquibase-validate-" + firstLiquibaseParams.schemaName());

		liquibaseValidateRef.tell(Liquibase.Start.INSTANCE);

		return Behaviors
			.receive(Command.class)
			.onMessage(LiquibaseResponseWrapper.class, msg -> {
				if (msg.response() instanceof Liquibase.Success) {
					return onStart(liquibaseParamsList, replyTo, context);
				}
				else if (msg.response() instanceof Liquibase.Error error) {
					replyTo.tell(
						new Error(ExceptionUtil.generateStackTrace(error.exception())));
					return Behaviors.stopped();
				}
				else {
					return Behaviors.ignore();
				}
			})
			.onMessageEquals(Timeout.INSTANCE, () -> onTimeout(replyTo))
			.build();

	}

}
