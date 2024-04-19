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
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;

public class Schema {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}

	private static Behavior<Command> rollback(
		ActorContext<Command> context, DatasourceLiquibaseService service, String schemaName) {
		return Behaviors.receive(Command.class)
			.onMessage(Rollback.class, (msg) -> {

				try {
					service.rollbackRunLiquibaseMigration(schemaName);
					context.getLog().info("schema {} rollback", schemaName);
				}
				catch (Exception e) {
					context.getLog().error(e.getMessage(), e);
				}

				msg.replyTo().tell(new Schema.Success(schemaName));

				return Behaviors.stopped();

			})
			.build();
	}

	public sealed interface Response {}
	public record Success(String schemaName) implements Response {}
	public record Error(String message) implements Response {}

	public record Params(String virtualHost, String schemaName) {}

	public static Behavior<Command> create(
		DatasourceLiquibaseService service, Params params, ActorRef<Response> replyTo) {
		return Behaviors.setup(context -> initial(context, service, params, replyTo));
	}

	public static Behavior<Command> createRollback(
		DatasourceLiquibaseService service, String schemaName) {
		return Behaviors.setup(context -> rollback(context, service, schemaName));
	}

	public record Rollback(ActorRef<Response> replyTo) implements Command {}

	private static Behavior<Command> initial(
		ActorContext<Command> context, DatasourceLiquibaseService service,
		Params params, ActorRef<Response> replyTo) {
		return Behaviors.receive(Command.class)
			.onMessageEquals(Start.INSTANCE, () -> onStart(context, service, params, replyTo))
			.build();
	}

	private static Behavior<Command> onStart(
		ActorContext<Command> context, DatasourceLiquibaseService service,
		Params params, ActorRef<Response> replyTo) {

		try {
			service.runInitialization(params.schemaName(), params.virtualHost(), true);
			replyTo.tell(new Success(params.schemaName()));
		}
		catch (Exception e) {
			replyTo.tell(new Error(e.getMessage()));
		}

		return Behaviors.stopped();

	}


}
