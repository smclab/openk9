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

import io.openk9.common.util.VertxUtil;
import io.openk9.tenantmanager.service.TenantService;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;

public class Tenant {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}
	private record TenantCreated(io.openk9.tenantmanager.model.Tenant tenant) implements Command {}

	private static Behavior<Command> initial(
		ActorContext<Command> context, TenantService service, ActorRef<Response> replyTo,
		String virtualHost, String schemaName, String liquibaseSchemaName,
		String realmName, String clientId, String clientSecret) {

		return Behaviors.receive(Command.class)
			.onMessageEquals(Start.INSTANCE, () -> onStart(
				context, service, virtualHost, schemaName,
				liquibaseSchemaName, realmName, clientId, clientSecret
			))
			.onMessage(TenantCreated.class,
				msg -> onTenantCreated(
					context, replyTo, msg.tenant()
				)
			)
			.onMessage(TenantError.class,
				msg -> onTenantError(
					context, replyTo, msg.exception()
				)
			)
			.build();

	}

	public sealed interface Response {}
	public record Success(
		io.openk9.tenantmanager.model.Tenant tenant) implements Response {}
	public record Error(String message) implements Response {}

	public static Behavior<Command> create(
		TenantService service, ActorRef<Response> replyTo, String virtualHost,
		String schemaName, String liquibaseSchemaName, String realmName,
		String clientId, String clientSecret) {

		return Behaviors.setup(
			context -> initial(
				context, service, replyTo, virtualHost, schemaName, liquibaseSchemaName, realmName,
				clientId, clientSecret
			)
		);

	}

	private static Behavior<Command> onTenantError(
		ActorContext<Command> context, ActorRef<Response> replyTo,
		TenantException exception) {

		context.getLog().error("Tenant not created.", exception);

		replyTo.tell(new Error(exception.getMessage()));

		return Behaviors.stopped();

	}

	private static Behavior<Command> onStart(
		ActorContext<Command> context, TenantService service,
		String virtualHost, String schemaName, String liquibaseSchemaName,
		String realmName, String clientId, String clientSecret) {

		io.openk9.tenantmanager.model.Tenant tenant =
			new io.openk9.tenantmanager.model.Tenant();

		tenant.setLiquibaseSchemaName(liquibaseSchemaName);
		tenant.setSchemaName(schemaName);
		tenant.setVirtualHost(virtualHost);
		tenant.setRealmName(realmName);
		tenant.setClientId(clientId);
		tenant.setClientSecret(clientSecret);

		VertxUtil.runOnContext(
			() -> service
				.persist(tenant)
				.onItem()
				.invoke(t -> context.getSelf().tell(new TenantCreated(t)))
				.onFailure()
				.invoke(t -> context.getSelf().tell(new TenantError(new TenantException(t))))
		);

		return Behaviors.same();

	}

	private static Behavior<Command> onTenantCreated(
		ActorContext<Command> context, ActorRef<Response> replyTo,
		io.openk9.tenantmanager.model.Tenant tenant) {

		context.getLog().info("tenant created with id: {}", tenant.getId());

		replyTo.tell(new Success(tenant));

		return Behaviors.stopped();

	}

	private record TenantError(TenantException exception) implements Command {}


}
