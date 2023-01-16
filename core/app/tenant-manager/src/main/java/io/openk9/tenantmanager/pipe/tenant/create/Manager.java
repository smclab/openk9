package io.openk9.tenantmanager.pipe.tenant.create;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Manager {

	public sealed interface Command {}
	private record ResponseWrapper(Object response) implements Command {}

	public sealed interface Response {}
	public record Success(
		String virtualHost, String schemaName,
		String liquibaseSchemaName, String realmName,
		String clientId, String clientSecret
	) implements Response {}


	public static Behavior<Command> create(
		String virtualHost, String schemaName,
		DatasourceLiquibaseService service,
		KeycloakAdminClientConfig keycloakAdminClientConfig,
		ActorRef<Manager.Response> responseActorRef) {

		return Behaviors.setup(context -> {

			ActorRef<Keycloak.Response> keycloakResponse =
				context.messageAdapter(Keycloak.Response.class, ResponseWrapper::new);

			ActorRef<Keycloak.Command> keycloakRef =
				context.spawn(
					Keycloak.create(
						keycloakAdminClientConfig,
						new Keycloak.Params(virtualHost, schemaName),
						keycloakResponse
					),
					"keycloak-" + schemaName
				);

			ActorRef<Schema.Response> schemaResponse =
				context.messageAdapter(Schema.Response.class, ResponseWrapper::new);

			ActorRef<Schema.Command> schemaRef =
				context.spawn(
					Schema.create(
						service,
						new Schema.Params(virtualHost, schemaName),
						schemaResponse
					),
					"schema-" + schemaName
				);

			schemaRef.tell(Schema.Start.INSTANCE);
			keycloakRef.tell(Keycloak.Start.INSTANCE);

			return initial(context, responseActorRef, service, keycloakAdminClientConfig, schemaName, List.of(), 2);

		});

	}

	private static Behavior<Command> initial(
		ActorContext<Command> context, ActorRef<Manager.Response> responseActorRef, DatasourceLiquibaseService service,
		KeycloakAdminClientConfig keycloakAdminClientConfig, String schemaName, List<ResponseWrapper> responses,
		int messageCount) {
		return Behaviors.receive(Command.class)
			.onMessage(ResponseWrapper.class, response -> {

				List<ResponseWrapper> newResponses = new ArrayList<>(responses);
				newResponses.add(response);
				if (newResponses.size() == messageCount) {
					return finalize(
						context, responseActorRef, service, keycloakAdminClientConfig,
						List.copyOf(newResponses), schemaName);
				}
				else {
					return initial(
						context, responseActorRef, service, keycloakAdminClientConfig,
						schemaName, List.copyOf(newResponses), messageCount);
				}

			})
			.build();
	}

	private static Behavior<Command> finalize(
		ActorContext<Command> context, ActorRef<Manager.Response> responseActorRef,
		DatasourceLiquibaseService service,
		KeycloakAdminClientConfig keycloakAdminClientConfig,
		List<ResponseWrapper> responseList, String schemaName) {

		Map<Boolean, List<Object>> responsePartitioned =
			responseList
				.stream()
				.map(ResponseWrapper::response)
				.collect(Collectors.partitioningBy(
					response ->
						response instanceof Keycloak.Success ||
						response instanceof Schema.Success));

		List<Object> errorList = responsePartitioned.get(false);

		boolean errorOccurred = errorList != null && !errorList.isEmpty();

		if (!errorOccurred) {
			List<Object> successList = responsePartitioned.get(true);
			String virtualHost = null;
			String realmName = null;
			String clientId = null;
			String clientSecret = null;
			String liquibaseSchemaName = null;
			for (Object o : successList) {
				if (o instanceof Keycloak.Success) {
					Keycloak.Success success =(Keycloak.Success)o;
					virtualHost = success.virtualHost();
					realmName = success.realmName();
					clientId = success.clientId();
					clientSecret = success.clientSecret();
					context.getLog().info("Keycloak: " + success);
				}
				else if (o instanceof Schema.Success) {
					Schema.Success success =(Schema.Success)o;
					liquibaseSchemaName = success.schemaName() + "_liquibase";
					context.getLog().info("Schema: " + success);
				}
			}
			responseActorRef.tell(
				new Success(
					virtualHost, schemaName, liquibaseSchemaName, realmName,
					clientId, clientSecret));
		}
		else {
			for (Object o : errorList) {
				if (o instanceof Keycloak.Error) {
					Keycloak.Error error =(Keycloak.Error)o;
					context.getLog().error("Keycloak: " + error);
					ActorRef<Keycloak.Command> keycloakRollbackRef =
						context.spawn(
							Keycloak.createRollback(
								keycloakAdminClientConfig, schemaName), "keycloak-rollback-" + schemaName);
					keycloakRollbackRef.tell(Keycloak.Start.INSTANCE);
				}
				else if (o instanceof Schema.Error) {
					Schema.Error error =(Schema.Error)o;
					context.getLog().error("Schema: " + error);
					ActorRef<Schema.Command> schemaRollbackRef =
						context.spawn(
							Schema.createRollback(
								service, schemaName), "schema-rollback-" + schemaName);
					schemaRollbackRef.tell(Schema.Start.INSTANCE);
				}
			}
		}

		return Behaviors.stopped();
	}
}
