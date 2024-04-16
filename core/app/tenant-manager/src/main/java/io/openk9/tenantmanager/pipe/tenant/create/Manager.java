package io.openk9.tenantmanager.pipe.tenant.create;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.app.manager.grpc.AppManager;
import io.openk9.tenantmanager.config.KeycloakContext;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Manager {

	public sealed interface Command {}
	private record ResponseWrapper(Object response) implements Command {}
	private record TenantResponseWrapper(Tenant.Response response) implements Command {}
	private enum Delay implements Command {INSTANCE}

	public sealed interface Response {}
	public record Success(io.openk9.tenantmanager.model.Tenant tenant) implements Response {}


	public static Behavior<Command> create(
		String virtualHost, String schemaName,
		DatasourceLiquibaseService liquibaseService, TenantService tenantService,
		AppManager appManager,
		KeycloakContext keycloakContext,
		ActorRef<Manager.Response> responseActorRef) {

		return Behaviors.setup(context -> {

			ActorRef<Keycloak.Response> keycloakResponse =
				context.messageAdapter(Keycloak.Response.class, ResponseWrapper::new);

			ActorRef<Keycloak.Command> keycloakRef =
				context.spawn(
					Keycloak.create(
						keycloakContext,
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
						liquibaseService,
						new Schema.Params(virtualHost, schemaName),
						schemaResponse
					),
					"schema-" + schemaName
				);

			ActorRef<Ingress.Response> ingressResponse =
					context.messageAdapter(Ingress.Response.class, ResponseWrapper::new);

			ActorRef<Ingress.Command> ingressRef =
				context.spawn(
					Ingress.create(
						appManager,
						schemaName,
						virtualHost,
						ingressResponse
					),
					"ingress-" + schemaName
				);


			schemaRef.tell(Schema.Start.INSTANCE);
			keycloakRef.tell(Keycloak.Start.INSTANCE);
			ingressRef.tell(Ingress.Start.INSTANCE);

			return initial(
				context, responseActorRef, liquibaseService, tenantService, appManager,
				keycloakContext, schemaName, virtualHost, List.of(), 3);

		});

	}

	public static Behavior<Command> createRollback(
		String schemaName,
		DatasourceLiquibaseService service,
		KeycloakContext keycloakContext) {

		return Behaviors.setup(context -> {

			_tellRollback(
				context, service, keycloakContext.getKeycloakAdminClientConfig(),
				schemaName);

			context.scheduleOnce(Duration.ofSeconds(5), context.getSelf(), Delay.INSTANCE);

			return Behaviors.receiveMessage(msg -> Behaviors.stopped());

		});

	}

	private static Behavior<Command> initial(
		ActorContext<Command> context, ActorRef<Manager.Response> responseActorRef,
		DatasourceLiquibaseService liquibaseService, TenantService tenantService,
		AppManager appManager,
		KeycloakContext keycloakContext,
		String schemaName, String virtualHost, 
		List<ResponseWrapper> responses,
		int messageCount) {
		
		return Behaviors.receive(Command.class)
			.onMessage(ResponseWrapper.class, response -> {

				List<ResponseWrapper> newResponses = new ArrayList<>(responses);
				newResponses.add(response);
				if (newResponses.size() == messageCount) {
					return finalize(
						context, responseActorRef, liquibaseService, tenantService, appManager,
						keycloakContext, List.copyOf(newResponses), schemaName, virtualHost);
				}
				else {
					return initial(
						context, responseActorRef, liquibaseService, tenantService, appManager,
						keycloakContext, schemaName, virtualHost, List.copyOf(newResponses),
						messageCount);
				}

			})
			.build();
	}

	private static Behavior<Command> finalize(
		ActorContext<Command> context,
		ActorRef<Response> responseActorRef,
		DatasourceLiquibaseService liquibaseService,
		TenantService tenantService,
		AppManager appManager,
		KeycloakContext keycloakContext,
		List<ResponseWrapper> responseList,
		String schemaName,
		String virtualHost) {

		Map<Boolean, List<Object>> responsePartitioned = responseList
			.stream()
			.map(ResponseWrapper::response)
			.collect(Collectors.partitioningBy(response ->
				response instanceof Keycloak.Success ||
				response instanceof Schema.Success ||
				response instanceof Ingress.Success)
			);

		List<Object> errorList = responsePartitioned.get(false);

		boolean errorOccurred = errorList != null && !errorList.isEmpty();

		if (!errorOccurred) {
			List<Object> successList = responsePartitioned.get(true);
			String realmName = null;
			String clientId = null;
			String clientSecret = null;
			String liquibaseSchemaName = null;
			for (Object o : successList) {
				if (o instanceof Keycloak.Success success) {
					realmName = success.realmName();
					clientId = success.clientId();
					clientSecret = success.clientSecret();
					context.getLog().info("Keycloak: " + success);
				}
				else if (o instanceof Schema.Success success) {
					liquibaseSchemaName = success.schemaName() + "_liquibase";
					context.getLog().info("Schema: " + success);
				}
				else if (o instanceof Ingress.Success) {
					context.getLog().info("Ingress created");
				}
			}

			ActorRef<Tenant.Response> tenantResponseRef =
				context.messageAdapter(
					Tenant.Response.class, TenantResponseWrapper::new);

			ActorRef<Tenant.Command> tenantRef =
				context.spawn(
					Tenant.create(
						tenantService,
						tenantResponseRef,
						virtualHost,
						schemaName,
						liquibaseSchemaName,
						realmName,
						clientId,
						clientSecret
					),
					"tenant-" + schemaName
				);

			tenantRef.tell(Tenant.Start.INSTANCE);

			return Behaviors
				.receive(Command.class)
				.onMessage(TenantResponseWrapper.class, response -> {
					Tenant.Response tenantResponse = response.response();
					if (tenantResponse instanceof Tenant.Success) {
						Tenant.Success success = (Tenant.Success)tenantResponse;
						responseActorRef.tell(new Success(success.tenant()));
					}
					else {
						Tenant.Error error = (Tenant.Error)tenantResponse;

						context.getLog().error("Tenant: " + error);

						_tellRollback(
							context, liquibaseService,
							keycloakContext.getKeycloakAdminClientConfig(),
							schemaName);

					}
					return Behaviors.stopped();
				})
				.build();
		}
		else {

			if (errorList.size() >= 1) {

				for (Object errorResponse : errorList) {

					if (errorResponse instanceof Keycloak.Error) {
						ActorRef<Schema.Command> schemaRollbackRef =
						context.spawn(
							Schema.createRollback(
							liquibaseService, schemaName), "schema-rollback-" + schemaName);
						schemaRollbackRef.tell(Schema.Start.INSTANCE);
					}
					else if (errorResponse instanceof Schema.Error) {
						ActorRef<Keycloak.Command> keycloakRollbackRef =
						context.spawn(
							Keycloak.createRollback(
								keycloakContext.getKeycloakAdminClientConfig(),
						schemaName), "keycloak-rollback-" + schemaName);
						keycloakRollbackRef.tell(Keycloak.Start.INSTANCE);
					} 
					else if (errorResponse instanceof Ingress.Error) {
						ActorRef<Ingress.Command> ingressRollbackRef =
						context.spawn(
							Ingress.rollback(appManager, schemaName, virtualHost),
						"ingress-rollback-" + schemaName);
						ingressRollbackRef.tell(Ingress.Start.INSTANCE);
					}
				}

			}

			for (Object o : errorList) {

				if (o instanceof Keycloak.Error error) {
					context.getLog().error("Keycloak: " + error);
				}
				else if (o instanceof Schema.Error error) {
					context.getLog().error("Schema: " + error);
				}
				else if (o instanceof Ingress.Error error) {
					context.getLog().error("Ingress: " + error);
				}

			}
		}

		return Behaviors.stopped();
	}

	private static void _tellRollback(
		ActorContext<Command> context,
		DatasourceLiquibaseService liquibaseService,
		KeycloakAdminClientConfig keycloakAdminClientConfig,
		String schemaName) {

		ActorRef<Schema.Command> schemaRollbackRef =
			context.spawn(
				Schema.createRollback(
					liquibaseService, schemaName),
				"schema-rollback-" + schemaName);

		schemaRollbackRef.tell(Schema.Start.INSTANCE);

		ActorRef<Keycloak.Command> keycloakRollbackRef =
			context.spawn(
				Keycloak.createRollback(
					keycloakAdminClientConfig, schemaName),
				"keycloak-rollback-" + schemaName);

		keycloakRollbackRef.tell(Keycloak.Start.INSTANCE);

	}


}
