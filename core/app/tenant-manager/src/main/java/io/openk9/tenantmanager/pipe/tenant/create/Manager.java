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
import io.openk9.app.manager.grpc.AppManager;
import io.openk9.tenantmanager.config.KeycloakContext;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Manager {

	public sealed interface Command {}
	private record ResponseWrapper(Object response) implements Command {}
	private record TenantResponseWrapper(Tenant.Response response) implements Command {}

	public sealed interface Response {}
	public record Success(io.openk9.tenantmanager.model.Tenant tenant) implements Response {}

	public static Behavior<Command> create(
		String virtualHost, String schemaName,
		DatasourceLiquibaseService liquibaseService, TenantService tenantService,
		AppManager appManager,
		KeycloakContext keycloakContext,
		ActorRef<Manager.Response> replyTo) {

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
				context, replyTo, liquibaseService, tenantService, appManager,
				keycloakContext, schemaName, virtualHost, List.of(), 3);

		});

	}

	private static Behavior<Command> initial(
		ActorContext<Command> context,
		ActorRef<Manager.Response> replyTo,
		DatasourceLiquibaseService liquibaseService,
		TenantService tenantService,
		AppManager appManager,
		KeycloakContext keycloakContext,
		String schemaName,
		String virtualHost,
		List<ResponseWrapper> responses,
		int messageCount) {

		return Behaviors.receive(Command.class)
			.onMessage(ResponseWrapper.class, response -> {

				List<ResponseWrapper> newResponses = new ArrayList<>(responses);
				newResponses.add(response);
				if (newResponses.size() == messageCount) {
					return finalize(
						context, replyTo, liquibaseService, tenantService, appManager,
						keycloakContext, List.copyOf(newResponses), schemaName, virtualHost);
				}
				else {
					return initial(
						context, replyTo, liquibaseService, tenantService, appManager,
						keycloakContext, schemaName, virtualHost, List.copyOf(newResponses),
						messageCount);
				}

			})
			.build();
	}

	private static Behavior<Command> finalize(
		ActorContext<Command> context,
		ActorRef<Response> replyTo,
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
					context.getLog().info("Keycloak: {}", success);
				}
				else if (o instanceof Schema.Success success) {
					liquibaseSchemaName = success.schemaName() + "_liquibase";
					context.getLog().info("Schema: {}", success);
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
					if (tenantResponse instanceof Tenant.Success success) {
						replyTo.tell(new Success(success.tenant()));
					}
					else {
						Tenant.Error error = (Tenant.Error)tenantResponse;

						context.getLog().error("Tenant: {}", error);

						compensateAll(
							context,
							appManager,
							liquibaseService,
							keycloakContext,
							schemaName,
							virtualHost
						);

						replyTo.tell(Error.INSTANCE);
					}
					return Behaviors.stopped();
				})
				.build();
		}
		else {
			var errors = EnumSet.noneOf(Operations.class);

			for (Object obj : errorList) {

				if (obj instanceof Keycloak.Error error) {
					context.getLog().error("Keycloak: {}", error);
					errors.add(Operations.KEYCLOAK);
				}
				else if (obj instanceof Schema.Error error) {
					context.getLog().error("Schema: {}", error);
					errors.add(Operations.SCHEMA);
				}
				else if (obj instanceof Ingress.Error error) {
					context.getLog().error("Ingress: {}", error);
					errors.add(Operations.INGRESS);
				}
			}

			var compensations = EnumSet.complementOf(errors);

			for (Operations compensation : compensations) {
				switch (compensation) {
					case INGRESS -> compensateIngress(context, appManager, schemaName, virtualHost);
					case KEYCLOAK -> compensateKeycloak(context, keycloakContext, schemaName);
					case SCHEMA -> compensateSchema(context, liquibaseService, schemaName);
				}
			}

			return Behaviors.receive(Command.class)
				.onMessageEquals(
					CompensationResponse.INSTANCE,
					() -> onCompensate(replyTo, compensations.size())
				)
				.build();
		}

	}

	private static Behavior<Command> onCompensate(ActorRef<Response> replyTo, int remaining) {
		if (remaining > 0) {
			return onCompensate(replyTo, remaining - 1);
		}

		replyTo.tell(Error.INSTANCE);

		return Behaviors.stopped();
	}

	private static void compensateIngress(
		ActorContext<Command> context,
		AppManager appManager,
		String schemaName,
		String virtualHost) {

		var replyTo = context.messageAdapter(
			Ingress.Response.class,
			res -> CompensationResponse.INSTANCE
		);

		ActorRef<Ingress.Command> ref =
			context.spawn(
				Ingress.rollback(appManager, schemaName, virtualHost, replyTo),
				"ingress-rollback-" + schemaName
			);

		ref.tell(Ingress.Start.INSTANCE);
	}

	private static void compensateKeycloak(
		ActorContext<Command> context,
		KeycloakContext keycloakContext,
		String schemaName) {

		var replyTo = context.messageAdapter(
			Keycloak.Response.class,
			res -> CompensationResponse.INSTANCE
		);

		ActorRef<Keycloak.Command> ref =
			context.spawn(
				Keycloak.createRollback(
					keycloakContext.getKeycloakAdminClientConfig(), schemaName),
				"keycloak-rollback-" + schemaName
			);

		ref.tell(new Keycloak.Rollback(replyTo));
	}

	private static void compensateSchema(
		ActorContext<Command> context,
		DatasourceLiquibaseService liquibaseService,
		String schemaName) {

		var replyTo = context.messageAdapter(
			Schema.Response.class,
			res -> CompensationResponse.INSTANCE
		);

		ActorRef<Schema.Command> ref =
			context.spawn(
				Schema.createRollback(
					liquibaseService, schemaName),
				"schema-rollback-" + schemaName);

		ref.tell(new Schema.Rollback(replyTo));
	}

	private static void compensateAll(
		ActorContext<Command> context,
		AppManager appManager,
		DatasourceLiquibaseService liquibaseService,
		KeycloakContext keycloakContext,
		String schemaName,
		String virtualHost) {

		compensateIngress(context, appManager, schemaName, virtualHost);
		compensateKeycloak(context, keycloakContext, schemaName);
		compensateSchema(context, liquibaseService, schemaName);
	}

	public enum Error implements Response {
		INSTANCE
	}

	private enum CompensationResponse implements Command {
		INSTANCE
	}

	private enum Operations {
		INGRESS,
		KEYCLOAK,
		SCHEMA
	}

}
