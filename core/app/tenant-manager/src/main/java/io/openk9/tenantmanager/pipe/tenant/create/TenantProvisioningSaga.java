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
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

public class TenantProvisioningSaga extends AbstractBehavior<TenantProvisioningSaga.Command> {

	private final String virtualHost;
	private final String schemaName;
	private final ActorRef<Response> replyTo;
	private final ProvisioningFactory factory;
	// Accumulators
	private final List<Object> collectedResponses = new ArrayList<>();
	// Context Data
	private String issuerUri;
	private String clientId;
	private String clientSecret;
	private String liquibaseSchemaName;
	private int compensationCounter = 0;

	private TenantProvisioningSaga(
		ActorContext<Command> context,
		String virtualHost,
		String schemaName,
		ActorRef<Response> replyTo,
		ProvisioningFactory factory) {

		super(context);
		this.virtualHost = virtualHost;
		this.schemaName = schemaName;
		this.replyTo = replyTo;
		this.factory = factory;

		var realmAdapter = context.messageAdapter(Realm.Response.class, ResponseWrapper::new);
		var schemaAdapter = context.messageAdapter(Schema.Response.class, ResponseWrapper::new);
		var ingressAdapter = context.messageAdapter(Ingress.Response.class, ResponseWrapper::new);

		ActorRef<Realm.Command> realm = context.spawnAnonymous(factory.realm(
			virtualHost,
			schemaName,
			realmAdapter
		));

		ActorRef<Schema.Command> schema = context.spawnAnonymous(factory.schema(
			virtualHost,
			schemaName,
			schemaAdapter
		));

		ActorRef<Ingress.Command> ingress = context.spawnAnonymous(factory.ingress(
			virtualHost,
			schemaName,
			ingressAdapter
		));

		schema.tell(Schema.Start.INSTANCE);
		realm.tell(Realm.Start.INSTANCE);
		ingress.tell(Ingress.Start.INSTANCE);
	}

	public static Behavior<Command> create(
		String virtualHost,
		String schemaName,
		ActorRef<Response> replyTo) {

		return Behaviors.setup(ctx -> new TenantProvisioningSaga(
			ctx,
			virtualHost,
			schemaName,
			replyTo,
			new DefaultProvisioningFactory()
		));
	}

	public static Behavior<Command> create(
		String virtualHost,
		String schemaName,
		ActorRef<Response> replyTo,
		ProvisioningFactory factory) {

		return Behaviors.setup(ctx -> new TenantProvisioningSaga(
			ctx,
			virtualHost,
			schemaName,
			replyTo,
			factory
		));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder().onMessage(ResponseWrapper.class, this::onParallelResponse)
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

	private Behavior<Command> onCompensationComplete() {
		getContext().getLog().info(
			"Compensation #{} done.",
			Operations.values().length - compensationCounter);

		compensationCounter--;
		if (compensationCounter <= 0) {
			replyTo.tell(Error.INSTANCE);
			return Behaviors.stopped();
		}

		return handleCompensations();
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
		var tenantAdapter = getContext().messageAdapter(
			Tenant.Response.class,
			TenantResponseWrapper::new
		);

		Behavior<Tenant.Command> tenantBehavior = factory.tenant(
			virtualHost,
			schemaName,
			liquibaseSchemaName,
			issuerUri,
			clientId,
			clientSecret,
			tenantAdapter
		);

		ActorRef<Tenant.Command> tenantRef = getContext().spawn(
			tenantBehavior,
			"tenant-" + schemaName
		);
		tenantRef.tell(Tenant.Start.INSTANCE);

		return newReceiveBuilder().onMessage(TenantResponseWrapper.class, this::onTenantResponse)
			.build();
	}

	private Behavior<Command> onParallelResponse(ResponseWrapper wrapper) {
		collectedResponses.add(wrapper.response());

		if (collectedResponses.size() == 3) {
			return processParallelResults();
		}

		return this;
	}

	private Behavior<Command> onTenantResponse(TenantResponseWrapper wrapper) {
		if (wrapper.response() instanceof Tenant.Success(TenantResponseDTO tenant)) {
			replyTo.tell(new Success(tenant));
			return Behaviors.stopped();
		}
		else {
			Tenant.Error error = (Tenant.Error) wrapper.response();
			getContext().getLog().error("Tenant creation failed: {}", error);

			return compensateAll();
		}
	}

	private Behavior<Command> processParallelResults() {
		Map<Boolean, List<Object>> partitioned =
			collectedResponses.stream().collect(Collectors.partitioningBy(r ->
				r instanceof Realm.Success || r instanceof Schema.Success ||
				r instanceof Ingress.Success));

		List<Object> errors = partitioned.get(false);

		extractContextData(partitioned.get(true));

		if (errors == null || errors.isEmpty()) {
			return onOperationsSuccess();
		}
		else {
			return onOperationsFailure(errors);
		}
	}

	private Behavior<Command> startCompensations(EnumSet<Operations> compensations) {
		this.compensationCounter = compensations.size();

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

	private Receive<Command> handleCompensations() {

		return newReceiveBuilder().onMessageEquals(
			CompensationResponse.INSTANCE,
			this::onCompensationComplete
		).build();
	}

	public sealed interface Command {}
	record ResponseWrapper(Object response) implements Command {}
	record TenantResponseWrapper(Tenant.Response response) implements Command {}
	enum CompensationResponse implements Command { INSTANCE }

	public sealed interface Response {}
	public enum Error implements Response { INSTANCE }
	public record Success(TenantResponseDTO tenant) implements Response {}

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

		Behavior<Tenant.Command> tenant(
			String virtualHost,
			String schemaName,
			String liquibaseSchemaName,
			String issuerUri,
			String clientId,
			String clientSecret,
			ActorRef<Tenant.Response> replyTo);

		// Compensation Behaviors

		Behavior<Ingress.Command> ingressRollback(
			String schemaName,
			String virtualHost,
			ActorRef<Ingress.Response> replyTo);

		Behavior<Realm.Command> realmRollback(String schemaName, ActorRef<Realm.Response> replyTo);

		Behavior<Schema.Command> schemaRollback(
			String schemaName,
			ActorRef<Schema.Response> replyTo);

	}

	private static class DefaultProvisioningFactory implements ProvisioningFactory {
		@Override
		public Behavior<Ingress.Command> ingress(String v, String s, ActorRef<Ingress.Response> r) {
			return Ingress.create(v, s, r);
		}

		@Override
		public Behavior<Ingress.Command> ingressRollback(
			String s,
			String v,
			ActorRef<Ingress.Response> r) {
			return Ingress.rollback(v, s, r);
		}

		@Override
		public Behavior<Realm.Command> realm(String v, String s, ActorRef<Realm.Response> r) {
			return Realm.create(v, s, r);
		}

		@Override
		public Behavior<Realm.Command> realmRollback(String s, ActorRef<Realm.Response> r) {
			return Realm.createRollback(s, r);
		}

		@Override
		public Behavior<Schema.Command> schema(String v, String s, ActorRef<Schema.Response> r) {
			return Schema.create(v, s, r);
		}

		@Override
		public Behavior<Schema.Command> schemaRollback(String s, ActorRef<Schema.Response> r) {
			return Schema.createRollback(s, r);
		}

		@Override
		public Behavior<Tenant.Command> tenant(
			String v,
			String s,
			String l,
			String i,
			String c,
			String sc,
			ActorRef<Tenant.Response> r) {
			return Tenant.create(v, s, l, i, c, sc, r);
		}
	}

	private enum Operations {
		INGRESS,
		REALM,
		SCHEMA
	}

}
