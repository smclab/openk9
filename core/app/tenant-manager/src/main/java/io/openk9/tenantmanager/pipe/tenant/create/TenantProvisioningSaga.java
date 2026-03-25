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
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.InvalidTenantNameException;
import io.openk9.tenantmanager.service.TenantProvisioningService;
import io.openk9.tenantmanager.service.TenantRealmService;
import io.openk9.tenantmanager.service.dto.OAuth2Settings;
import io.openk9.tenantmanager.util.TenantNameValidator;

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
	private final boolean keycloakAvailable;
	private final ActorRef<Response> replyTo;
	private final ProvisioningFactory factory;

	// Accumulators

	private final List<Object> collectedResponses = new ArrayList<>();
	private int expectedResponses;
	private int compensationCounter = 0;

	// Context Data

	private String tenantName;
	private final OAuth2Settings oAuth2Settings;

	private String issuerUri;
	private String clientId;
	private String clientSecret;

	private boolean realmProvisioned = false;

	private TenantProvisioningSaga(
		ActorContext<Command> context,
		String virtualHost,
		String tenantName,
		OAuth2Settings oAuth2Settings,
		SecurityConfiguration securityConfiguration,
		ActorRef<Response> replyTo,
		ProvisioningFactory factory) {

		super(context);
		this.virtualHost = virtualHost;
		this.oAuth2Settings = oAuth2Settings;
		this.securityConfiguration = securityConfiguration;
		this.replyTo = replyTo;
		this.factory = factory;
		this.keycloakAvailable =
			TenantRealmService.isKeycloakAvailable();

		this.tenantName = tenantName;
		if (tenantName == null) {
			context.pipeToSelf(
				TenantProvisioningService
					.generateRandomSchemaName(),
				(res, exc) -> {
					if (res != null) {
						return new NameResolved(res);
					}
					else {
						return ResolutionFailed.INSTANCE;
					}
				}
			);
		}
		else {
			// Name already known — trigger provisioning
			context.getSelf().tell(
				new NameResolved(tenantName));
		}
	}

	/**
	 * Creates a provisioning saga behavior using the default
	 * provisioning factory.
	 *
	 * @param virtualHost           the tenant virtual host
	 * @param tenantName            the tenant name, or null to
	 *                              auto-generate
	 * @param oAuth2Settings        external OAuth2 settings, or
	 *                              null for Keycloak
	 * @param securityConfiguration the tenant's security model
	 * @param replyTo               the actor to reply to
	 * @return the saga behavior
	 */
	public static Behavior<Command> create(
		String virtualHost,
		String tenantName,
		OAuth2Settings oAuth2Settings,
		SecurityConfiguration securityConfiguration,
		ActorRef<Response> replyTo) {

		return create(
			virtualHost, tenantName, oAuth2Settings,
			securityConfiguration,
			replyTo, new DefaultProvisioningFactory()
		);
	}

	/**
	 * Creates a provisioning saga behavior with the given
	 * factory.
	 *
	 * @param virtualHost           the tenant virtual host
	 * @param tenantName            the tenant name, or null to
	 *                              auto-generate
	 * @param oAuth2Settings        external OAuth2 settings, or
	 *                              null for Keycloak
	 * @param securityConfiguration the tenant's security model
	 * @param replyTo               the actor to reply to
	 * @param provisioningFactory   factory for provisioner
	 *                              actors
	 * @return the saga behavior
	 */
	public static Behavior<Command> create(
		String virtualHost,
		String tenantName,
		OAuth2Settings oAuth2Settings,
		SecurityConfiguration securityConfiguration,
		ActorRef<Response> replyTo,
		ProvisioningFactory provisioningFactory) {

		return Behaviors.setup(ctx -> {

			// Fail-fast: OAuth2 is needed but there is
			// no way to provide it.
			if (requiresKeycloak(
				oAuth2Settings, securityConfiguration)
				&& !TenantRealmService
					.isKeycloakAvailable()) {

				ctx.getLog().error(
					"Tenant requires OAuth2 "
					+ "authentication but neither "
					+ "Keycloak is available nor "
					+ "external oAuth2Settings were "
					+ "provided "
					+ "(securityConfiguration={})",
					securityConfiguration);
				replyTo.tell(Error.INSTANCE);
				return Behaviors.stopped();
			}

			return new TenantProvisioningSaga(
				ctx, virtualHost, tenantName,
				oAuth2Settings, securityConfiguration,
				replyTo, provisioningFactory
			);
		});
	}

	@Override
	public Receive<Command> createReceive() {

		return newReceiveBuilder()
			.onMessage(
				NameResolved.class, this::onNameResolved)
			.onMessage(
				ResolutionFailed.class,
				this::onResolutionFailed)
			.onMessage(
				OperationResponse.class,
				this::onParallelResponse)
			.build();
	}

	private Behavior<Command> onNameResolved(
		NameResolved msg) {

		this.tenantName = msg.name();

		try {
			TenantNameValidator.validate(tenantName);
		}
		catch (InvalidTenantNameException e) {
			getContext().getLog().error(
				"Invalid tenant name '{}': {}",
				tenantName, e.getMessage());
			replyTo.tell(Error.INSTANCE);
			return Behaviors.stopped();
		}

		startProvisioning();
		return this;
	}

	private Behavior<Command> onResolutionFailed(
		ResolutionFailed msg) {

		getContext().getLog().error(
			"Failed to resolve prerequisites");
		replyTo.tell(Error.INSTANCE);
		return Behaviors.stopped();
	}

	// --- Parallel provisioning ---

	private void startProvisioning() {

		// Schema and Ingress always run
		var schemaAdapter = getContext().messageAdapter(
			SchemaProvisioner.Response.class,
			OperationResponse::new);
		var ingressAdapter = getContext().messageAdapter(
			IngressProvisioner.Response.class,
			OperationResponse::new);

		ActorRef<SchemaProvisioner.Command> schema =
			getContext().spawnAnonymous(factory
				.schema(virtualHost, tenantName,
					schemaAdapter));
		ActorRef<IngressProvisioner.Command> ingress =
			getContext().spawnAnonymous(factory
				.ingress(virtualHost, tenantName,
					ingressAdapter));

		schema.tell(SchemaProvisioner.Start.INSTANCE);
		ingress.tell(IngressProvisioner.Start.INSTANCE);

		this.expectedResponses = 2;

		if (oAuth2Settings != null) {
			// External OAuth2 — set credentials directly,
			// no realm actor needed.
			this.issuerUri = oAuth2Settings.issuerUri();
			this.clientId = oAuth2Settings.clientId();
			this.clientSecret = oAuth2Settings.clientSecret();
		}
		else if (requiresKeycloak(
			oAuth2Settings, securityConfiguration)
			&& keycloakAvailable) {
			// Keycloak available — spawn realm provisioner
			var realmAdapter = getContext().messageAdapter(
				RealmProvisioner.Response.class,
				OperationResponse::new);

			ActorRef<RealmProvisioner.Command> realm =
				getContext().spawnAnonymous(
					factory.realm(
						virtualHost, tenantName,
						realmAdapter));
			realm.tell(RealmProvisioner.Start.INSTANCE);
			this.expectedResponses++;
		}
	}

	private Behavior<Command> onParallelResponse(
		OperationResponse wrapper) {

		collectedResponses.add(wrapper.response());

		if (collectedResponses.size() == expectedResponses) {
			return processParallelResults();
		}

		return this;
	}

	private Behavior<Command> processParallelResults() {
		Map<Boolean, List<Object>> partitioned =
			collectedResponses.stream()
				.collect(Collectors.partitioningBy(r ->
					r instanceof RealmProvisioner.Success
					|| r instanceof SchemaProvisioner.Success
					|| r instanceof IngressProvisioner.Success)
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

	private void extractContextData(
		List<Object> successList) {

		for (Object o : successList) {
			if (o instanceof RealmProvisioner.Success s) {
				this.issuerUri = s.issuerUri();
				this.clientId = s.clientId();
				this.clientSecret = s.clientSecret();
				this.realmProvisioned = true;

				getContext().getLog().info(
					"Realm provisioned: {}", s);
			}
			else if (
				o instanceof SchemaProvisioner.Success s) {

				getContext().getLog().info(
					"Schema provisioned: {}", s);
			}
			else if (
				o instanceof IngressProvisioner.Success) {

				getContext().getLog().info(
					"Ingress provisioned");
			}
		}
	}

	// --- Success & persistence ---

	private Behavior<Command> onOperationsSuccess() {
		Tenant tenant = new Tenant();

		tenant.setTenantName(tenantName);
		tenant.setVirtualHost(virtualHost);
		tenant.setIssuerUri(issuerUri);
		tenant.setClientId(clientId);
		tenant.setClientSecret(clientSecret);
		tenant.setSecurityConfiguration(securityConfiguration);
		tenant.setRealmProvisioned(realmProvisioned);

		getContext().pipeToSelf(
			TenantProvisioningService.createEntity(tenant),
			PersistResponse::new
		);

		return newReceiveBuilder()
			.onMessage(
				PersistResponse.class,
				this::onTenantResponse)
			.build();
	}

	private Behavior<Command> onTenantResponse(
		PersistResponse response) {

		if (response.tenantException() != null) {
			Throwable error = response.tenantException();
			getContext().getLog().error(
				"Tenant creation failed.", error);

			return compensateAll();
		}
		else if (response.tenant() == null) {
			getContext().getLog().error(
				"Tenant creation failed with "
				+ "unknown issue.");

			return compensateAll();
		}
		else {
			TenantResponseDTO tenant = response.tenant();
			replyTo.tell(new Success(tenant));

			return Behaviors.stopped();
		}
	}

	// --- Compensation (saga rollback) ---

	private Behavior<Command> compensateAll() {
		return startCompensations(
			EnumSet.allOf(Operations.class));
	}

	private Behavior<Command> onOperationsFailure(
		List<Object> errorList) {

		var errors = EnumSet.noneOf(Operations.class);

		for (Object obj : errorList) {
			if (obj instanceof RealmProvisioner.Error e) {
				getContext().getLog().error(
					"Realm failed: {}", e);
				errors.add(Operations.REALM);
			}
			else if (
				obj instanceof SchemaProvisioner.Error e) {

				getContext().getLog().error(
					"Schema failed: {}", e);
				errors.add(Operations.SCHEMA);
			}
			else if (
				obj instanceof IngressProvisioner.Error e) {

				getContext().getLog().error(
					"Ingress failed: {}", e);
				errors.add(Operations.INGRESS);
			}
		}

		return startCompensations(
			EnumSet.complementOf(errors));
	}

	private Behavior<Command> startCompensations(
		EnumSet<Operations> compensations) {

		this.compensationCounter = compensations.size();

		getContext().getLog().info(
			"Starting compensation for {} operations.",
			compensationCounter);

		if (compensationCounter == 0) {
			replyTo.tell(Error.INSTANCE);
			return Behaviors.stopped();
		}

		for (Operations op : compensations) {
			switch (op) {
				case INGRESS -> {
					var ref = getContext().messageAdapter(
						IngressProvisioner.Response.class,
						r -> {
							getContext().getLog().debug(
								"Ingress Rolled Back");
							return CompensationResponse
								.INSTANCE;
						}
					);
					getContext().spawnAnonymous(
						factory.ingressRollback(
							tenantName, virtualHost, ref))
						.tell(
							IngressProvisioner
								.Start.INSTANCE);
				}
				case REALM -> {
					if (!realmProvisioned) {
						// Realm was never spawned — skip
						compensationCounter--;
						break;
					}
					var ref = getContext().messageAdapter(
						RealmProvisioner.Response.class,
						r -> {
							getContext().getLog().debug(
								"Realm Rolled Back");
							return CompensationResponse
								.INSTANCE;
						}
					);
					getContext().spawnAnonymous(
						factory.realmRollback(
							tenantName, ref))
						.tell(
							RealmProvisioner
								.Rollback.INSTANCE);
				}
				case SCHEMA -> {
					var ref = getContext().messageAdapter(
						SchemaProvisioner.Response.class,
						r -> {
							getContext().getLog().debug(
								"Schema Rolled Back");
							return CompensationResponse
								.INSTANCE;
						}
					);
					getContext().spawnAnonymous(
						factory.schemaRollback(
							tenantName, ref))
						.tell(
							SchemaProvisioner
								.Rollback.INSTANCE);
				}
			}
		}

		return handleCompensations();
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
			Operations.values().length
				- compensationCounter);

		compensationCounter--;
		if (compensationCounter <= 0) {
			replyTo.tell(Error.INSTANCE);
			return Behaviors.stopped();
		}

		return handleCompensations();
	}

	/**
	 * Whether Keycloak is needed to provision the identity
	 * layer for this tenant: no external OAuth2 settings
	 * were provided and the security model requires OAuth2.
	 */
	private static boolean requiresKeycloak(
		OAuth2Settings oAuth2Settings,
		SecurityConfiguration securityConfiguration) {

		return oAuth2Settings == null
			&& securityConfiguration.requiresOAuth2();
	}

	// --- Types ---

	private enum CompensationResponse implements Command {
		INSTANCE
	}

	/** Signals that provisioning failed. */
	public enum Error implements Response {
		INSTANCE
	}

	private enum Operations {
		INGRESS,
		REALM,
		SCHEMA
	}

	/** Marker interface for saga commands. */
	public sealed interface Command {}

	/**
	 * Factory for creating provisioner actor behaviors.
	 * Implementations provide both provisioning and rollback
	 * behaviors for realm, schema, and ingress.
	 */
	public interface ProvisioningFactory {

		// Provisioning Behaviors

		/** Creates an ingress provisioner behavior. */
		Behavior<IngressProvisioner.Command> ingress(
			String virtualHost,
			String tenantName,
			ActorRef<IngressProvisioner.Response> replyTo);

		/** Creates a realm provisioner behavior. */
		Behavior<RealmProvisioner.Command> realm(
			String virtualHost,
			String tenantName,
			ActorRef<RealmProvisioner.Response> replyTo);

		/** Creates a schema provisioner behavior. */
		Behavior<SchemaProvisioner.Command> schema(
			String virtualHost,
			String tenantName,
			ActorRef<SchemaProvisioner.Response> replyTo);

		// Compensation Behaviors

		/** Creates an ingress rollback behavior. */
		Behavior<IngressProvisioner.Command> ingressRollback(
			String tenantName,
			String virtualHost,
			ActorRef<IngressProvisioner.Response> replyTo);

		/** Creates a realm rollback behavior. */
		Behavior<RealmProvisioner.Command> realmRollback(
			String tenantName,
			ActorRef<RealmProvisioner.Response> replyTo);

		/** Creates a schema rollback behavior. */
		Behavior<SchemaProvisioner.Command> schemaRollback(
			String tenantName,
			ActorRef<SchemaProvisioner.Response> replyTo);
	}

	/** Marker interface for saga responses. */
	public sealed interface Response {}

	private static class DefaultProvisioningFactory
		implements ProvisioningFactory {

		@Override
		public Behavior<IngressProvisioner.Command> ingress(
			String v, String s,
			ActorRef<IngressProvisioner.Response> r) {

			return IngressProvisioner.create(v, s, r);
		}

		@Override
		public Behavior<IngressProvisioner.Command>
			ingressRollback(
			String s, String v,
			ActorRef<IngressProvisioner.Response> r) {

			return IngressProvisioner.rollback(v, s, r);
		}

		@Override
		public Behavior<RealmProvisioner.Command> realm(
			String v, String s,
			ActorRef<RealmProvisioner.Response> r) {

			return RealmProvisioner.create(v, s, r);
		}

		@Override
		public Behavior<RealmProvisioner.Command>
			realmRollback(
			String s,
			ActorRef<RealmProvisioner.Response> r) {

			return RealmProvisioner.createRollback(s, r);
		}

		@Override
		public Behavior<SchemaProvisioner.Command> schema(
			String v, String s,
			ActorRef<SchemaProvisioner.Response> r) {

			return SchemaProvisioner.create(v, s, r);
		}

		@Override
		public Behavior<SchemaProvisioner.Command>
			schemaRollback(
			String s,
			ActorRef<SchemaProvisioner.Response> r) {

			return SchemaProvisioner.createRollback(s, r);
		}

	}

	private record NameResolved(String name)
		implements Command {}

	private record OperationResponse(Object response)
		implements Command {}

	private enum ResolutionFailed implements Command {
		INSTANCE
	}

	/** Successful provisioning result with tenant data. */
	public record Success(TenantResponseDTO tenant)
		implements Response {}

	private record PersistResponse(
		TenantResponseDTO tenant,
		Throwable tenantException)
		implements Command {}

}
