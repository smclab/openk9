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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.openk9.tenantmanager.dto.TenantResponseDTO;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;

public class Manager {

	public sealed interface Command {}
	private record ResponseWrapper(Object response) implements Command {}
	private record TenantResponseWrapper(Tenant.Response response) implements Command {}

	public sealed interface Response {}
	public record Success(TenantResponseDTO tenant) implements Response {}

	public static Behavior<Command> create(
		String virtualHost, String schemaName,
		ActorRef<Manager.Response> replyTo) {

		return Behaviors.setup(context -> {

			ActorRef<Realm.Response> realmResponse =
				context.messageAdapter(Realm.Response.class, ResponseWrapper::new);

			ActorRef<Realm.Command> realm =
				context.spawn(
					Realm.create(
						virtualHost,
						schemaName, // todo change with realmname
						realmResponse
					),
					"realm-" + schemaName
				);

			ActorRef<Schema.Response> schemaResponse =
				context.messageAdapter(Schema.Response.class, ResponseWrapper::new);

			ActorRef<Schema.Command> schema =
				context.spawn(
					Schema.create(virtualHost, schemaName, schemaResponse),
					"schema-" + schemaName
				);

			ActorRef<Ingress.Response> ingressResponse =
					context.messageAdapter(Ingress.Response.class, ResponseWrapper::new);

			ActorRef<Ingress.Command> ingressRef =
				context.spawn(
					Ingress.create(schemaName, virtualHost, ingressResponse),
					"ingress-" + schemaName
				);


			schema.tell(Schema.Start.INSTANCE);
			realm.tell(Realm.Start.INSTANCE);
			ingressRef.tell(Ingress.Start.INSTANCE);

			return initial(
				context, replyTo, schemaName, virtualHost, List.of(), 3);

		});

	}

	private static Behavior<Command> initial(
		ActorContext<Command> context,
		ActorRef<Manager.Response> replyTo,
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
						context, replyTo, List.copyOf(newResponses),
						schemaName, virtualHost);
				}
				else {
					return initial(
						context, replyTo, schemaName, virtualHost,
						List.copyOf(newResponses), messageCount);
				}

			})
			.build();
	}

	private static Behavior<Command> finalize(
		ActorContext<Command> context,
		ActorRef<Response> replyTo,
		List<ResponseWrapper> responseList,
		String schemaName,
		String virtualHost) {

		Map<Boolean, List<Object>> responsePartitioned = responseList
			.stream()
			.map(ResponseWrapper::response)
			.collect(Collectors.partitioningBy(response ->
				response instanceof Realm.Success ||
				response instanceof Schema.Success ||
				response instanceof Ingress.Success)
			);

		List<Object> errorList = responsePartitioned.get(false);

		boolean errorOccurred = errorList != null && !errorList.isEmpty();

		if (!errorOccurred) {
			List<Object> successList = responsePartitioned.get(true);
			String issuerUri = null;
			String clientId = null;
			String clientSecret = null;
			String liquibaseSchemaName = null;
			for (Object o : successList) {
				if (o instanceof Realm.Success success) {
					issuerUri = success.issuerUri();
					clientId = success.clientId();
					clientSecret = success.clientSecret();
					context.getLog().info("Realm: {}", success);
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
						tenantResponseRef,
						virtualHost,
						schemaName,
						liquibaseSchemaName,
						issuerUri,
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

						compensateAll(context, schemaName, virtualHost);

						replyTo.tell(Error.INSTANCE);
					}
					return Behaviors.stopped();
				})
				.build();
		}
		else {
			var errors = EnumSet.noneOf(Operations.class);

			for (Object obj : errorList) {

				if (obj instanceof Realm.Error error) {
					context.getLog().error("Keycloak: {}", error);
					errors.add(Operations.REALM);
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
					case INGRESS -> compensateIngress(context, schemaName, virtualHost);
					case REALM -> compensateRealm(context, schemaName);
					case SCHEMA -> compensateSchema(context, schemaName);
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
		ActorContext<Command> context, String schemaName, String virtualHost) {

		var replyTo = context.messageAdapter(
			Ingress.Response.class,
			res -> CompensationResponse.INSTANCE
		);

		ActorRef<Ingress.Command> ref =
			context.spawn(
				Ingress.rollback(schemaName, virtualHost, replyTo),
				"ingress-rollback-" + schemaName
			);

		ref.tell(Ingress.Start.INSTANCE);
	}

	private static void compensateRealm(
		ActorContext<Command> context, String schemaName) {

		var replyTo = context.messageAdapter(
			Realm.Response.class,
			res -> CompensationResponse.INSTANCE
		);

		ActorRef<Realm.Command> ref =
			context.spawn(
				Realm.createRollback(replyTo, schemaName),
				"realm-rollback-" + schemaName
			);

		ref.tell(Realm.Rollback.INSTANCE);
	}

	private static void compensateSchema(
		ActorContext<Command> context, String schemaName) {

		var replyTo = context.messageAdapter(
			Schema.Response.class,
			res -> CompensationResponse.INSTANCE
		);

		ActorRef<Schema.Command> ref =
			context.spawn(
				Schema.createRollback(replyTo, schemaName),
				"schema-rollback-" + schemaName);

		ref.tell(Schema.Rollback.INSTANCE);
	}

	private static void compensateAll(
		ActorContext<Command> context, String schemaName, String virtualHost) {

		compensateIngress(context, schemaName, virtualHost);
		compensateRealm(context, schemaName);
		compensateSchema(context, schemaName);
	}

	public enum Error implements Response {
		INSTANCE
	}

	private enum CompensationResponse implements Command {
		INSTANCE
	}

	private enum Operations {
		INGRESS,
		REALM,
		SCHEMA
	}

}
