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

import io.openk9.tenantmanager.config.KeycloakContext;
import io.openk9.tenantmanager.config.KeycloakDefaultRealmRepresentationFactory;
import io.openk9.tenantmanager.pipe.tenant.create.factory.RealmRepresentationFactory;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfigUtil;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public class Keycloak {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}

	public static Behavior<Command> createRollback(
		KeycloakAdminClientConfig config, String realmName) {
		return Behaviors.setup(context -> rollback(context, config, realmName));
	}

	public sealed interface Response {}
	public record Success(String clientId, String clientSecret, String virtualHost, String realmName, String username, String password) implements Response {}

	private static Behavior<Command> rollback(
		ActorContext<Command> context,
		KeycloakAdminClientConfig clientConfig,
		String realmName) {

		return Behaviors.receive(Command.class)
			.onMessage(Rollback.class, (msg) -> {

				try (var keycloakClient = createKeycloakClient(clientConfig)) {
					keycloakClient.realms().realm(realmName).remove();
					context.getLog().info("realm {} rollback", realmName);
				}
				catch (Exception e) {
					context.getLog().error(e.getMessage(), e);
				}

				msg.replyTo().tell(SuccessRollback.INSTANCE);

				return Behaviors.stopped();

			})
			.build();
	}
	public record Error(String message) implements Response {}

	public record Params(String virtualHost, String realmName) {}

	public static Behavior<Command> create(
		KeycloakContext keycloakContext, Params params, ActorRef<Response> replyTo) {
		return Behaviors.setup(context -> {

			org.keycloak.admin.client.Keycloak keycloakClient =
				createKeycloakClient(keycloakContext.getKeycloakAdminClientConfig());

			return initial(
				context, replyTo, keycloakClient,
				keycloakContext.getKeycloakDefaultRealmRepresentationFactory(),
				params);
		});
	}

	public enum SuccessRollback implements Response {
		INSTANCE
	}

	public record Rollback(ActorRef<Response> replyTo) implements Command {}

	private static Behavior<Command> initial(
		ActorContext<Command> context, ActorRef<Response> replyTo,
		org.keycloak.admin.client.Keycloak keycloakClient,
		KeycloakDefaultRealmRepresentationFactory keycloakDefaultRealmRepresentationFactory,
		Params params) {
		return Behaviors.receive(Command.class)
			.onMessageEquals(
				Start.INSTANCE,
				() -> onStart(
					context, replyTo, keycloakClient,
					keycloakDefaultRealmRepresentationFactory, params))
			.build();
	}

	private static Behavior<Command> onStart(
		ActorContext<Command> context, ActorRef<Response> replyTo,
		org.keycloak.admin.client.Keycloak keycloakClient,
		KeycloakDefaultRealmRepresentationFactory keycloakDefaultRealmRepresentationFactory,
		Params params) {

		RealmRepresentation realmRepresentation =
			RealmRepresentationFactory.createRealmRepresentation(
				params.virtualHost, params.realmName,
				keycloakDefaultRealmRepresentationFactory.getDefaultRealmRepresentation());

		try {
			keycloakClient.realms().create(realmRepresentation);
			String clientId = realmRepresentation.getClients().get(0).getName();
			UserRepresentation userRepresentation =
				realmRepresentation.getUsers().get(0);
			replyTo.tell(
				new Success(
					clientId, null, params.virtualHost(), params.realmName(),
					userRepresentation.getUsername(),
					userRepresentation.getCredentials().get(0).getValue()
				)
			);
		}
		catch (Exception e) {
			replyTo.tell(new Error(e.getMessage()));
		}

		return Behaviors.stopped();
	}

	private static org.keycloak.admin.client.Keycloak createKeycloakClient(
		KeycloakAdminClientConfig config) {

		KeycloakAdminClientConfigUtil.validate(config);

		if (config.serverUrl().isEmpty()) {
			throw new IllegalStateException("keycloak serverUrl is empty");
		}

		KeycloakBuilder keycloakBuilder = KeycloakBuilder
			.builder()
			.clientId(config.clientId())
			.clientSecret(config.clientSecret().orElse(null))
			.grantType(config.grantType().asString())
			.username(config.username().orElse(null))
			.password(config.password().orElse(null))
			.realm(config.realm())
			.serverUrl(config.serverUrl().get())
			.scope(config.scope().orElse(null));

		return keycloakBuilder.build();

	}

}
