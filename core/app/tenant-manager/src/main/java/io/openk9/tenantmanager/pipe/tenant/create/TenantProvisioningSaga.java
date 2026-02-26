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
import io.openk9.tenantmanager.model.SecurityConfiguration;
import io.openk9.tenantmanager.service.TenantProvisioningService;
import io.openk9.tenantmanager.service.dto.OAuth2Settings;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

public class TenantProvisioningSaga
	extends AbstractBehavior<TenantProvisioningSaga.Command> {

	private final String virtualHost;
	private final SecurityConfiguration securityConfiguration;
	private final ActorRef<Response> replyTo;
	private final ProvisioningFactory factory;

	// Accumulators
	private final List<Object> collectedResponses = new ArrayList<>();
	private final OAuth2Settings oAuth2Settings;
	private final Boolean skipOAuth2;

	// Context Data
	private String schemaName;
	private String issuerUri;
	private String clientId;
	private String clientSecret;
	private String liquibaseSchemaName;
	private int compensationCounter = 0;

	private TenantProvisioningSaga(
		ActorContext<Command> context,
		String virtualHost,
		String schemaName,
		OAuth2Settings oAuth2Settings,
		Boolean skipOAuth2,
		SecurityConfiguration securityConfiguration,
		ActorRef<Response> replyTo,
		ProvisioningFactory factory) {

		super(context);
		this.virtualHost = virtualHost;
		this.schemaName = schemaName;
		this.oAuth2Settings = oAuth2Settings;
		this.skipOAuth2 = skipOAuth2;
		this.securityConfiguration = securityConfiguration;
		this.replyTo = replyTo;
		this.factory = factory;

		if (schemaName == null) {
			context.pipeToSelf(
				TenantProvisioningService.generateRandomSchemaName(),
				(res, exc) -> {
					if (res != null) {
						return new NameResolved(res);
					}
					else {
						return new OperationResponse(Error.INSTANCE);
					}
				}
			);
		}
		else {
			startProvisioning(context);
		}
	}

	public static Behavior<Command> create(
		String virtualHost,
		String schemaName,
		OAuth2Settings oAuth2Settings,
		Boolean skipOAuth2,
		SecurityConfiguration securityConfiguration,
		ActorRef<Response> replyTo) {

		return Behaviors.setup(ctx -> new TenantProvisioningSaga(
			ctx, virtualHost, schemaName,
			oAuth2Settings, skipOAuth2,
			securityConfiguration, replyTo,
			new DefaultProvisioningFactory()
		));
	}

	public static Behavior<Command> create(
		String virtualHost,
		String schemaName,
		OAuth2Settings oAuth2Settings,
		Boolean skipOAuth2,
		SecurityConfiguration securityConfiguration,
		ActorRef<Response> replyTo,
		ProvisioningFactory provisioningFactory) {

		return Behaviors.setup(ctx -> new TenantProvisioningSaga(
			ctx, virtualHost, schemaName,
			oAuth2Settings, skipOAuth2,
			securityConfiguration,
			replyTo,
			provisioningFactory
		));
	}

	@Override
	public Receive<Command> createReceive() {

		return newReceiveBuilder()
			.onMessage(OperationResponse.class, this::onParallelResponse)
			.onMessage(NameResolved.class, this::onNameResolved)
			.build();
	}

	private Behavior<Command> compensateAll() {
		return startCompensations(EnumSet.allOf(Operations.class));
	}

	private void extractContextData(List<Object> successList) {
		for (Object o : successList) {
			if (o instanceof Realm.Success s) {
				this.issuerUri = s.issuerUri();
				this.clientId = s.clientId();
				this.clientSecret = s.clientSecret();
				getContext().getLog().info("Realm provisioned: {}", s);
			}
			else if (o instanceof Schema.Success s) {
				this.liquibaseSchemaName = s.schemaName() + "_liquibase";
				getContext().getLog().info("Schema provisioned: {}", s);
			}
			else if (o instanceof Ingress.Success) {
				getContext().getLog().info("Ingress provisioned");
			}
		}
	}

	private Receive<Command> handleCompensations() {

		return newReceiveBuilder().onMessageEquals(
			CompensationResponse.INSTANCE,
			this::onCompensationComplete
		).build();
	}

	private Behavior<Command> onCompensationComplete() {
		getContext().getLog().info(
			"Compensation #{} done.",
			Operations.values().length - compensationCounter
		);

		compensationCounter--;
		if (compensationCounter <= 0) {
			replyTo.tell(Error.INSTANCE);
			return Behaviors.stopped();
		}

		return handleCompensations();
	}

	private Behavior<Command> onNameResolved(NameResolved msg) {
		this.schemaName = msg.name();
		startProvisioning(getContext());
		return this;
	}

	private Behavior<Command> onOperationsFailure(List<Object> errorList) {
		var errors = EnumSet.noneOf(Operations.class);

		for (Object obj : errorList) {
			if (obj instanceof Realm.Error error) {
				getContext().getLog().error("Realm failed: {}", error);
				errors.add(Operations.REALM);
			}
			else if (obj instanceof Schema.Error error) {
				getContext().getLog().error("Schema failed: {}", error);
				errors.add(Operations.SCHEMA);
			}
			else if (obj instanceof Ingress.Error error) {
				getContext().getLog().error("Ingress failed: {}", error);
				errors.add(Operations.INGRESS);
			}
		}

		return startCompensations(EnumSet.complementOf(errors));
	}

	private Behavior<Command> onOperationsSuccess() {
		io.openk9.tenantmanager.model.Tenant tenant =
			new io.openk9.tenantmanager.model.Tenant();

		tenant.setLiquibaseSchemaName(liquibaseSchemaName);
		tenant.setSchemaName(schemaName);
		tenant.setVirtualHost(virtualHost);
		tenant.setIssuerUri(issuerUri);
		tenant.setClientId(clientId);
		tenant.setClientSecret(clientSecret);
		tenant.setSecurityConfiguration(securityConfiguration);

		getContext().pipeToSelf(
			TenantProvisioningService.createEntity(tenant),
			PersistResponse::new
		);

		return newReceiveBuilder()
			.onMessage(PersistResponse.class, this::onTenantResponse)
			.build();
	}

	private Behavior<Command> onParallelResponse(OperationResponse wrapper) {
		collectedResponses.add(wrapper.response());

		if (collectedResponses.size() == 3) {
			return processParallelResults();
		}

		return this;
	}

	private Behavior<Command> onTenantResponse(PersistResponse response) {
		if (response.tenantException() != null) {
			Throwable error = response.tenantException();
			getContext().getLog().error("Tenant creation failed.", error);

			return compensateAll();
		}
		else if (response.tenant() == null) {
			getContext().getLog().error(
				"Tenant creation failed with unknown issue.");

			return compensateAll();
		}
		else {
			TenantResponseDTO tenant = response.tenant();
			replyTo.tell(new Success(tenant));

			return Behaviors.stopped();
		}
	}

	private Behavior<Command> processParallelResults() {
		Map<Boolean, List<Object>> partitioned = collectedResponses.stream()
			.collect(Collectors.partitioningBy(r ->
				r instanceof Realm.Success
				|| r instanceof Schema.Success
				|| r instanceof Ingress.Success)
			);

		List<Object> errors = partitioned.get(false);

		extractContextData(partitioned.get(true));

		if (errors == null || errors.isEmpty()) {
			return onOperationsSuccess();
		}
		else {
			return onOperationsFailure(errors);
		}
	}

	private Behavior<Command> startCompensations(
		EnumSet<Operations> compensations) {

		this.compensationCounter = compensations.size();

		getContext().getLog().info(
			"Starting compensation for {} operations.", compensationCounter);

		if (compensationCounter == 0) {
			replyTo.tell(Error.INSTANCE);
			return Behaviors.stopped();
		}

		for (Operations op : compensations) {
			switch (op) {
				case INGRESS -> {
					var replyTo = getContext().messageAdapter(
						Ingress.Response.class,
						r -> {
							getContext().getLog().debug("Ingress Rolled Back");
							return CompensationResponse.INSTANCE;
						}
					);
					getContext().spawnAnonymous(factory.ingressRollback(
							schemaName, virtualHost, replyTo))
						.tell(Ingress.Start.INSTANCE);
				}
				case REALM -> {
					var replyTo = getContext().messageAdapter(
						Realm.Response.class,
						r -> {
							getContext().getLog().debug("Realm Rolled Back");
							return CompensationResponse.INSTANCE;
						}
					);
					getContext().spawnAnonymous(factory.realmRollback(
							schemaName, replyTo))
						.tell(Realm.Rollback.INSTANCE);
				}
				case SCHEMA -> {
					var replyTo = getContext().messageAdapter(
						Schema.Response.class,
						r -> {
							getContext().getLog().debug("Schema Rolled Back");
							return CompensationResponse.INSTANCE;
						}
					);
					getContext().spawnAnonymous(
							factory.schemaRollback(schemaName, replyTo))
						.tell(Schema.Rollback.INSTANCE);
				}
			}
		}

		return handleCompensations();
	}

	private void startProvisioning(ActorContext<Command> context) {
		var schemaAdapter = context.messageAdapter(
			Schema.Response.class, OperationResponse::new);
		var ingressAdapter = context.messageAdapter(
			Ingress.Response.class, OperationResponse::new);

		if (oAuth2Settings != null) {
			// Skip Realm creation, simulate success
			context.getSelf().tell(new OperationResponse(new Realm.Success(
				oAuth2Settings.clientId(),
				oAuth2Settings.clientSecret(),
				virtualHost,
				oAuth2Settings.issuerUri(),
				null, null // username/password not needed if we have settings
			)));
		}
		else if (Boolean.TRUE.equals(skipOAuth2)) {
			// Skip Realm creation, simulate success with dummy values
			context.getSelf().tell(new OperationResponse(new Realm.Success(
				"DISABLED",
				"DISABLED",
				virtualHost,
				"DISABLED",
				null, null
			)));
		}
		else {
			var realmAdapter = context.messageAdapter(
				Realm.Response.class, OperationResponse::new);

			ActorRef<Realm.Command> realm = context.spawnAnonymous(factory
				.realm(virtualHost, schemaName, realmAdapter));
			realm.tell(Realm.Start.INSTANCE);
		}

		ActorRef<Schema.Command> schema = context.spawnAnonymous(factory
			.schema(virtualHost, schemaName, schemaAdapter));

		ActorRef<Ingress.Command> ingress = context.spawnAnonymous(factory
			.ingress(virtualHost, schemaName, ingressAdapter));

		schema.tell(Schema.Start.INSTANCE);
		ingress.tell(Ingress.Start.INSTANCE);
	}

	private enum CompensationResponse implements Command {
		INSTANCE
	}

	public enum Error implements Response {
		INSTANCE
	}

	private enum Operations {
		INGRESS,
		REALM,
		SCHEMA
	}

	public sealed interface Command {
	}

	public interface ProvisioningFactory {

		// Provisioning Behaviors

		Behavior<Ingress.Command> ingress(
			String virtualHost,
			String schemaName,
			ActorRef<Ingress.Response> replyTo);

		Behavior<Realm.Command> realm(
			String virtualHost,
			String schemaName,
			ActorRef<Realm.Response> replyTo);

		Behavior<Schema.Command> schema(
			String virtualHost,
			String schemaName,
			ActorRef<Schema.Response> replyTo);

		// Compensation Behaviors

		Behavior<Ingress.Command> ingressRollback(
			String schemaName,
			String virtualHost,
			ActorRef<Ingress.Response> replyTo);

		Behavior<Realm.Command> realmRollback(
			String schemaName, ActorRef<Realm.Response> replyTo);

		Behavior<Schema.Command> schemaRollback(
			String schemaName,
			ActorRef<Schema.Response> replyTo);
	}

	public sealed interface Response {
	}

	private static class DefaultProvisioningFactory
		implements ProvisioningFactory {

		@Override
		public Behavior<Ingress.Command> ingress(
			String v, String s, ActorRef<Ingress.Response> r) {

			return Ingress.create(v, s, r);
		}

		@Override
		public Behavior<Ingress.Command> ingressRollback(
			String s, String v, ActorRef<Ingress.Response> r) {

			return Ingress.rollback(v, s, r);
		}

		@Override
		public Behavior<Realm.Command> realm(
			String v, String s, ActorRef<Realm.Response> r) {

			return Realm.create(v, s, r);
		}

		@Override
		public Behavior<Realm.Command> realmRollback(
			String s, ActorRef<Realm.Response> r) {

			return Realm.createRollback(s, r);
		}

		@Override
		public Behavior<Schema.Command> schema(
			String v, String s, ActorRef<Schema.Response> r) {

			return Schema.create(v, s, r);
		}

		@Override
		public Behavior<Schema.Command> schemaRollback(
			String s, ActorRef<Schema.Response> r) {

			return Schema.createRollback(s, r);
		}

	}

	private record NameResolved(String name) implements Command {}

	private record OperationResponse(Object response) implements Command {}

	public record Success(TenantResponseDTO tenant) implements Response {}

	private record PersistResponse(
		TenantResponseDTO tenant, Throwable tenantException)
		implements Command {}

}
