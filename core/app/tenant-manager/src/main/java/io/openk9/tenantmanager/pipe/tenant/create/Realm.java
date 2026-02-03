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
import io.openk9.tenantmanager.service.TenantRealmService;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;

public class Realm {

	public sealed interface Command {}
	public enum Start implements Command { INSTANCE } 
	
	private record CreateRealmResponse(
		TenantRealmService.CreatedRealm createdRealm,
		Throwable error
	) implements Command {}
	
	private record DeleteRealmResponse(
		Void result,
		Throwable error
	) implements Command {}

	public enum Rollback implements Command { INSTANCE }

	public sealed interface Response {}

	public record Success(
		String clientId,
		String clientSecret,
		String virtualHost,
		String issuerUri,
		String username,
		String password
	) implements Response {}

	public record Error(String message) implements Response {}

	public enum SuccessRollback implements Response { INSTANCE }

	// Setup behaviors
	
	public static Behavior<Command> create(
		String virtualHost, String realmName, ActorRef<Response> replyTo) {

		return Behaviors.setup(context ->
			initial(context, replyTo, virtualHost, realmName));
	}

	public static Behavior<Command> createRollback(
		String realmName, ActorRef<Response> replyTo) {

		return Behaviors.setup(context -> rollback(context, replyTo, realmName));
	}

	// Creation behaviors
	
	private static Behavior<Command> initial(
		ActorContext<Command> context, ActorRef<Response> replyTo,
		String virtualHost, String realmName) {

		return Behaviors.receive(Command.class)
			.onMessageEquals(
				Start.INSTANCE,
				() -> onStart(context, replyTo, virtualHost, realmName))
			.build();
	}

	private static Behavior<Command> onStart(
		ActorContext<Command> context, ActorRef<Response> replyTo,
		String virtualHost, String realmName) {

		context.pipeToSelf(
			TenantProvisioningService.createRealm(virtualHost, realmName),
			CreateRealmResponse::new
		);

		return awaitCreateRealm(replyTo);
	}
	
	private static Behavior<Command> awaitCreateRealm(
		ActorRef<Response> replyTo) {

		return Behaviors.receive(Command.class)
			.onMessage(
				CreateRealmResponse.class,
				msg -> onCreateRealmResponse(replyTo, msg))
			.build();
	}

	private static Behavior<Command> onCreateRealmResponse(
		ActorRef<Response> replyTo, CreateRealmResponse message) {

		if (message.error() != null) {
			var e = message.error();
			replyTo.tell(new Error(e.getMessage()));
		}
		else {
			var createdRealm = message.createdRealm();

			replyTo.tell(
				new Success(
					createdRealm.clientId(),
					createdRealm.clientSecret(),
					createdRealm.virtualHost(),
					createdRealm.issuerUri(),
					createdRealm.username(),
					createdRealm.password()
				)
			);
		}

		return Behaviors.stopped();
	}

	// Deletion behaviors
	
	private static Behavior<Command> rollback(
		ActorContext<Command> context,
		ActorRef<Response> replyTo,
		String realmName) {

		return Behaviors.receive(Command.class)
			.onMessage(Rollback.class, (msg) -> {

				context.pipeToSelf(
					TenantProvisioningService.deleteRealm(realmName),
					DeleteRealmResponse::new
				);

				return awaitDeleteBehavior(replyTo);
			})
			.build();
	}

	private static Behavior<Command> awaitDeleteBehavior(
		ActorRef<Response> replyTo) {

		return Behaviors.receive(Command.class)
			.onMessage(
				DeleteRealmResponse.class,
				msg -> onDeleteRealmResponse(replyTo, msg)
			)
			.build();
	}


	private static Behavior<Command> onDeleteRealmResponse(
		ActorRef<Response> replyTo, DeleteRealmResponse message) {

		if (message.error() != null) {
			var e = message.error();
			replyTo.tell(new Error(e.getMessage()));
		}
		else {
			replyTo.tell(SuccessRollback.INSTANCE);
		}

		return Behaviors.stopped();
	}

}
