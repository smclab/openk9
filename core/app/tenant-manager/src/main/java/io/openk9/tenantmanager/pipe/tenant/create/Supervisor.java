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

import io.openk9.app.manager.grpc.AppManager;
import io.openk9.tenantmanager.config.KeycloakContext;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;

public class Supervisor {
	public sealed interface Command {}

	public record Start(
		String virtualHost, String schemaName,
		DatasourceLiquibaseService liquibaseService,
		TenantService tenantService,
		AppManager appManager,
		KeycloakContext keycloakContext,
		ActorRef<Supervisor.Response> replyTo) implements Command {}

	public record ResponseWrapper(
		Manager.Response response,
		ActorRef<Supervisor.Response> replyTo) implements Command {}

	public sealed interface Response {}
	public record Success(Tenant tenant) implements Response {}

	private static Behavior<Command> initial(ActorContext<Command> context) {

		return Behaviors
			.receive(Command.class)
			.onMessage(Start.class, start -> {

				ActorRef<Manager.Response> responseActorRef =
					context.messageAdapter(
						Manager.Response.class,
						param -> new ResponseWrapper(param, start.replyTo));

				context.spawn(
					Manager.create(
						start.virtualHost(),
						start.schemaName(),
						start.liquibaseService(),
						start.tenantService(),
						start.appManager(),
						start.keycloakContext(),
						responseActorRef
					),
					"manager-" + start.schemaName()
				);
				return Behaviors.same();
			})
			.onMessage(ResponseWrapper.class, responseWrapper -> {
				if (responseWrapper.response() instanceof Manager.Success) {
					Manager.Success response = (Manager.Success) responseWrapper.response;
					responseWrapper.replyTo.tell(new Success(response.tenant()));
				}
				else if (responseWrapper.response() == Manager.Error.INSTANCE) {
					responseWrapper.replyTo.tell(Error.INSTANCE);
				}
				return Behaviors.same();
			})
			.build();
	}

	public static Behavior<Command> create() {
		return Behaviors.setup(Supervisor::initial);
	}

	public enum Error implements Response {
		INSTANCE
	}

}
