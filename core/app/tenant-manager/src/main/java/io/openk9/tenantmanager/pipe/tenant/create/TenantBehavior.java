package io.openk9.tenantmanager.pipe.tenant.create;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.tenantmanager.model.BackgroundProcess;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.pipe.tenant.create.message.KeycloakMessage;
import io.openk9.tenantmanager.pipe.tenant.create.message.SchemaMessage;
import io.openk9.tenantmanager.pipe.tenant.create.message.TenantMessage;
import io.openk9.tenantmanager.service.BackgroundProcessService;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.openk9.tenantmanager.util.VertxUtil;
import io.quarkus.runtime.util.ExceptionUtil;
import org.keycloak.admin.client.Keycloak;

import java.util.UUID;

public class TenantBehavior extends AbstractBehavior<TenantMessage> {

	public TenantBehavior(
		UUID requestId,
		ActorContext<TenantMessage> context,
		String virtualHost,
		Keycloak keycloak, TenantService tenantService, BackgroundProcessService backgroundProcessService,
		DatasourceLiquibaseService liquibaseService) {
		super(context);
		this.requestId = requestId;
		this.virtualHost = virtualHost;
		this.keycloak = keycloak;
		this.tenantService = tenantService;
		this.backgroundProcessService = backgroundProcessService;
		this.liquibaseService = liquibaseService;
	}

	public static Behavior<TenantMessage> create(
		UUID requestId, String virtualHost,
		Keycloak keycloak, TenantService tenantService,
		BackgroundProcessService backgroundProcessService,
		DatasourceLiquibaseService liquibaseService) {

		return Behaviors.setup(
			context -> new TenantBehavior(
				requestId, context, virtualHost,
				keycloak, tenantService, backgroundProcessService, liquibaseService
			)
		);
	}

	@Override
	public Receive<TenantMessage> createReceive() {
		return newReceiveBuilder()
			.onAnyMessage(this::onAnyMessage)
			.build();
	}

	private Behavior<TenantMessage> onAnyMessage(TenantMessage tenantMessage) {

		if (tenantMessage instanceof TenantMessage.Start) {
			return onStart((TenantMessage.Start)tenantMessage);
		}
		else if (tenantMessage instanceof TenantMessage.ProcessCreatedId) {
			return onProcessCreatedId((TenantMessage.ProcessCreatedId)tenantMessage);
		}
		else if (tenantMessage instanceof TenantMessage.Stop) {
			return onStop((TenantMessage.Stop)tenantMessage);
		}
		else if (tenantMessage instanceof TenantMessage.Error) {
			return onError((TenantMessage.Error)tenantMessage);
		}
		else if (tenantMessage instanceof TenantMessage.SchemaCreated) {
			onSchemaCreated((TenantMessage.SchemaCreated)tenantMessage);
		}
		else if (tenantMessage instanceof TenantMessage.RealmCreated) {
			onRealmCreated((TenantMessage.RealmCreated)tenantMessage);
		}

		if (this.realmName != null && this.schemaName != null) {

			Tenant tenant = new Tenant();
			tenant.setVirtualHost(this.virtualHost);
			tenant.setRealmName(this.realmName);
			tenant.setSchemaName(this.schemaName);
			tenant.setClientId(this.clientId);
			tenant.setClientSecret(this.clientSecret);

			VertxUtil.runOnContext(
				() -> tenantService
					.addTenant(tenant)
					.onItemOrFailure()
					.transformToUni((e, t) -> {

						if (t == null) {
							return backgroundProcessService.updateBackgroundProcess(
									this.processId, BackgroundProcess.Status.FINISHED,
									"Tenant created with virtualhost: " + virtualHost,
									"Tenant created")
								.invoke(this::_tellFinished);
						}

						return backgroundProcessService.updateBackgroundProcess(
								this.processId, BackgroundProcess.Status.FAILED,
								ExceptionUtil.generateStackTrace(t), "Tenant failed")
							.invoke(() -> getContext().getSelf().tell(new TenantMessage.Error(t)));

					})
			);

		}

		return Behaviors.same();

	}

	private Behavior<TenantMessage> onProcessCreatedId(
		TenantMessage.ProcessCreatedId tenantMessage) {

		this.processId = tenantMessage.processId();
		String realmName = tenantMessage.realmName();

		keycloakActorRef =
			getContext()
				.spawn(
					KeycloakBehavior.create(
						keycloak, requestId, backgroundProcessService,
						getContext().getSelf()), "keycloak-" + this.requestId
				);

		keycloakActorRef.tell(
			new KeycloakMessage.Start(
				getContext().getSelf(), virtualHost, realmName));

		schemaActorRef =
			getContext()
				.spawn(
					SchemaBehavior.create(
						liquibaseService, getContext().getSelf(),
						backgroundProcessService, requestId),
					"schema-" + this.requestId
				);

		schemaActorRef.tell(
			new SchemaMessage.Start(
				getContext().getSelf(), virtualHost, realmName));

		return Behaviors.same();
	}

	private Behavior<TenantMessage> onStop(TenantMessage.Stop stop) {
		getContext().getLog().info("Tenant finished " + this.requestId);
		return Behaviors.stopped();
	}

	private Behavior<TenantMessage> onError(TenantMessage.Error error) {
		Throwable exception = error.exception();

		VertxUtil.runOnContext(
			() -> backgroundProcessService.updateBackgroundProcess(
					this.processId, BackgroundProcess.Status.FAILED,
					ExceptionUtil.generateStackTrace(exception),
					"Process failed")
				.invoke(this::_tellError)
				.invoke(this::_tellFinished)
		);

		return Behaviors.same();
	}

	private void onSchemaCreated(
		TenantMessage.SchemaCreated schemaCreated) {
		this.schemaName = schemaCreated.schemaName();
	}

	private void onRealmCreated(
		TenantMessage.RealmCreated realmCreated) {
		this.realmName = realmCreated.realmName();
		this.clientId = realmCreated.clientId();
		this.clientSecret = realmCreated.clientSecret();
	}

	private Behavior<TenantMessage> onStart(TenantMessage.Start start) {

		VertxUtil.runOnContext(() ->
			backgroundProcessService
				.createBackgroundProcess(
					BackgroundProcess
						.builder()
						.name("Tenant creation")
						.status(BackgroundProcess.Status.IN_PROGRESS)
						.processId(this.requestId)
						.message("Starting tenant creation with virtual host " + this.virtualHost)
						.build()
				)
				.invoke(bp ->
					getContext()
						.getSelf()
						.tell(new TenantMessage.ProcessCreatedId(
							bp.getId(), start.realmName()))
				)
		);

		return Behaviors.same();

	}

	private void _tellError() {
		keycloakActorRef.tell(KeycloakMessage.Rollback.INSTANCE);
		schemaActorRef.tell(SchemaMessage.Rollback.INSTANCE);
	}

	private void _tellFinished() {
		keycloakActorRef.tell(KeycloakMessage.Stop.INSTANCE);
		schemaActorRef.tell(SchemaMessage.Stop.INSTANCE);
		getContext().getSelf().tell(TenantMessage.Stop.INSTANCE);
	}

	private final UUID requestId;
	private long processId;
	private String schemaName;
	private String realmName;
	private final String virtualHost;
	private String clientId;
	private String clientSecret;
	private ActorRef<SchemaMessage> schemaActorRef;
	private ActorRef<KeycloakMessage> keycloakActorRef;
	private final Keycloak keycloak;
	private final DatasourceLiquibaseService liquibaseService;
	private final TenantService tenantService;
	private final BackgroundProcessService backgroundProcessService;

}
