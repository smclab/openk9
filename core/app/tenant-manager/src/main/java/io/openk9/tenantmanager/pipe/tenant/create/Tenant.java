package io.openk9.tenantmanager.pipe.tenant.create;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.common.util.VertxUtil;
import io.openk9.tenantmanager.service.TenantService;

public class Tenant {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}
	private record TenantCreated(io.openk9.tenantmanager.model.Tenant tenant) implements Command {}
	private record TenantError(Throwable throwable) implements Command {}

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
					context, replyTo, msg.throwable()
				)
			)
			.build();

	}

	private static Behavior<Command> onTenantError(
		ActorContext<Command> context, ActorRef<Response> replyTo,
		Throwable throwable) {

		context.getLog().error(throwable.getMessage(), throwable);

		replyTo.tell(new Error(throwable.getMessage()));

		return Behaviors.stopped();

	}

	private static Behavior<Command> onTenantCreated(
		ActorContext<Command> context, ActorRef<Response> replyTo,
		io.openk9.tenantmanager.model.Tenant tenant) {

		context.getLog().info("tenant created with id: {}", tenant.getId());

		replyTo.tell(new Success(tenant));

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
				.invoke(t -> context.getSelf().tell(new TenantError(t)))
		);

		return Behaviors.same();

	}


}
