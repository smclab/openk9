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

import io.openk9.tenantmanager.service.TenantProvisioningService;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;

public class Schema {

	public sealed interface Command {}
	public enum Start implements Command { INSTANCE }
	public enum Rollback implements Command { INSTANCE }

	private record CreateSchemaResponse(Void result, Throwable error)
		implements Command {}

	private record DeleteSchemaResponse(Void result, Throwable error)
		implements Command {}

	public sealed interface Response {}

	public record Success(String schemaName) implements Response {}
	public record Error(String message) implements Response {}

	// Setup Behaviors

	public static Behavior<Command> create(
		String virtualHost, String schemaName, ActorRef<Response> replyTo) {

		return Behaviors.setup(context ->
			initial(context, virtualHost, schemaName, replyTo));
	}

	public static Behavior<Command> createRollback(
		ActorRef<Response> replyTo, String schemaName) {

		return Behaviors.setup(context ->
			rollback(context, replyTo, schemaName));
	}

	// Creation behaviors

	private static Behavior<Command> initial(
		ActorContext<Command> context,
		String virtualHost,
		String schemaName,
		ActorRef<Response> replyTo) {

		return Behaviors.receive(Command.class)
			.onMessageEquals(
				Start.INSTANCE,
				() -> onStart(context, virtualHost, schemaName, replyTo))
			.build();
	}

	private static Behavior<Command> onStart(
		ActorContext<Command> context,
		String virtualHost,
		String schemaName,
		ActorRef<Response> replyTo) {

		context.pipeToSelf(
			TenantProvisioningService.createSchema(virtualHost, schemaName),
			CreateSchemaResponse::new
		);

		return awaitCreateSchemaResponse(schemaName, replyTo);
	}

	private static Behavior<Command> awaitCreateSchemaResponse(
		String schemaName, ActorRef<Response> replyTo) {

		return Behaviors.receive(Command.class)
			.onMessage(
				CreateSchemaResponse.class,
				msg -> onCreateSchemaResponse(msg, schemaName, replyTo)
			)
			.build();
	}

	private static Behavior<Command> onCreateSchemaResponse(
		CreateSchemaResponse msg,
		String schemaName,
		ActorRef<Response> replyTo) {

		if (msg.error() != null) {
			replyTo.tell(new Error(msg.error().getMessage()));
		}
		else {
			replyTo.tell(new Success(schemaName));
		}

		return Behaviors.stopped();
	}

	// Rollback behaviors

	private static Behavior<Command> rollback(
		ActorContext<Command> context,
		ActorRef<Response> replyTo,
		String schemaName) {

		return Behaviors.receive(Command.class)
			.onMessageEquals(
				Rollback.INSTANCE,
				() -> onRollback(context, schemaName, replyTo))
			.build();
	}

	private static Behavior<Command> onRollback(
		ActorContext<Command> context,
		String schemaName,
		ActorRef<Response> replyTo) {

		context.pipeToSelf(
			TenantProvisioningService.deleteSchema(schemaName),
			DeleteSchemaResponse::new
		);

		return awaitDeleteSchemaResponse(schemaName, replyTo);
	}

	private static Behavior<Command> awaitDeleteSchemaResponse(
		String schemaName, ActorRef<Response> replyTo) {

		return Behaviors.receive(Command.class)
			.onMessage(
				DeleteSchemaResponse.class,
				(msg) -> onDeleteSchemaResponse(msg, schemaName, replyTo)
			)
			.build();
	}

	private static Behavior<Command> onDeleteSchemaResponse(
		DeleteSchemaResponse response,
		String schemaName,
		ActorRef<Response> replyTo) {

		if (response.error() != null) {
			replyTo.tell(new Error(response.error().getMessage()));
		}
		else {
			replyTo.tell(new Success(schemaName));
		}

		return Behaviors.stopped();
	}

}
