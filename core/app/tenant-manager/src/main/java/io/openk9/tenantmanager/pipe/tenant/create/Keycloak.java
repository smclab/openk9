package io.openk9.tenantmanager.pipe.tenant.create;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.tenantmanager.pipe.tenant.create.factory.RealmRepresentationFactory;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfig;
import io.quarkus.keycloak.admin.client.common.KeycloakAdminClientConfigUtil;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public class Keycloak {

	public sealed interface Command {}
	public enum Start implements Command {INSTANCE}

	public sealed interface Response {}
	public record Success(String clientId, String clientSecret, String virtualHost, String realmName, String username, String password) implements Response {}
	public record Error(String message) implements Response {}

	public record Params(String virtualHost, String realmName) {}

	public static Behavior<Command> create(
		KeycloakAdminClientConfig config, Params params, ActorRef<Response> replyTo) {
		return Behaviors.setup(context -> {

			org.keycloak.admin.client.Keycloak keycloakClient =
				createKeycloakClient(config);

			return initial(context, replyTo, keycloakClient, params);
		});
	}

	public static Behavior<Command> createRollback(
		KeycloakAdminClientConfig config, String realmName) {
		return Behaviors.setup(context -> {

			org.keycloak.admin.client.Keycloak keycloakClient =
				createKeycloakClient(config);

			return rollback(context, keycloakClient, realmName);
		});
	}

	private static Behavior<Command> rollback(
		ActorContext<Command> context,
		org.keycloak.admin.client.Keycloak keycloakClient, String realmName) {
		return Behaviors.receive(Command.class)
			.onMessageEquals(Start.INSTANCE, () -> {

				try {
					keycloakClient.realms().realm(realmName).remove();
				}
				catch (Exception e) {
					context.getLog().error(e.getMessage(), e);
				}

				return Behaviors.stopped();

			})
			.build();
	}

	private static Behavior<Command> initial(
		ActorContext<Command> context, ActorRef<Response> replyTo,
		org.keycloak.admin.client.Keycloak keycloakClient, Params params) {
		return Behaviors.receive(Command.class)
			.onMessageEquals(Start.INSTANCE, () -> onStart(context, replyTo, keycloakClient, params))
			.build();
	}

	private static Behavior<Command> onStart(
		ActorContext<Command> context, ActorRef<Response> replyTo,
		org.keycloak.admin.client.Keycloak keycloakClient, Params params) {

		RealmRepresentation realmRepresentation =
			RealmRepresentationFactory.createRealmRepresentation(
				params.virtualHost, params.realmName);

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

		if (config.serverUrl.isEmpty()) {
			throw new IllegalStateException("keycloak serverUrl is empty");
		}

		KeycloakBuilder keycloakBuilder = KeycloakBuilder
			.builder()
			.clientId(config.clientId)
			.clientSecret(config.clientSecret.orElse(null))
			.grantType(config.grantType.asString())
			.username(config.username.orElse(null))
			.password(config.password.orElse(null))
			.realm(config.realm)
			.serverUrl(config.serverUrl.get())
			.scope(config.scope.orElse(null));

		return keycloakBuilder.build();

	}

}
