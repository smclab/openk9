package io.openk9.tenantmanager.pipe.tenant.create;

import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.model.BackgroundProcess;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.pipe.tenant.create.message.KeycloakMessage;
import io.openk9.tenantmanager.pipe.tenant.create.message.SchemaMessage;
import io.openk9.tenantmanager.pipe.tenant.create.message.TenantMessage;
import io.openk9.tenantmanager.service.BackgroundProcessService;
import io.openk9.tenantmanager.service.TenantService;
import io.openk9.tenantmanager.util.VertxUtil;
import io.quarkus.runtime.util.ExceptionUtil;
import org.jboss.logging.Logger;

import java.util.UUID;

public class TenantBehavior implements TypedActor.Behavior<TenantMessage> {

	public TenantBehavior(
		UUID requestId, String virtualHost, String schemaName, String realmName,
		TenantService tenantService, BackgroundProcessService backgroundProcessService,
		TypedActor.Address<TenantMessage> self) {
		this.requestId = requestId;
		this.virtualHost = virtualHost;
		this.tenantService = tenantService;
		this.backgroundProcessService = backgroundProcessService;
		this.self = self;
		this.schemaName = schemaName;
		this.realmName = realmName;
	}

	public TenantBehavior(
		UUID requestId, String virtualHost, TenantService tenantService,
		BackgroundProcessService backgroundProcessService,
		TypedActor.Address<TenantMessage> self) {
		this(
			requestId, virtualHost, null, null,
			tenantService, backgroundProcessService, self);
	}

	@Override
	public TypedActor.Effect<TenantMessage> apply(TenantMessage message) {

		if (message instanceof TenantMessage.Start) {
			TenantMessage.Start start = (TenantMessage.Start)message;

			this.keycloak = start.keycloak();
			this.schema = start.schema();

			keycloak.tell(
				new KeycloakMessage.Start(
					self, virtualHost, start.realmName()));

			schema.tell(
				new SchemaMessage.Start(
					self, virtualHost, start.realmName()));

		}
		else if (message instanceof TenantMessage.RealmCreated) {
			TenantMessage.RealmCreated realmCreated = (TenantMessage.RealmCreated)message;
			this.realmName = realmCreated.realmName();
			this.clientId = realmCreated.clientId();
			this.clientSecret = realmCreated.clientSecret();
		}
		else if (message instanceof TenantMessage.SchemaCreated) {
			TenantMessage.SchemaCreated schemaCreated = (TenantMessage.SchemaCreated)message;
			this.schemaName = schemaCreated.schemaName();
		}
		else if (message instanceof TenantMessage.Error) {
			TenantMessage.Error error = (TenantMessage.Error)message;

			Throwable exception = error.exception();

			LOGGER.error(exception.getMessage(), exception);

			VertxUtil.runOnContext(
				() -> backgroundProcessService.updateBackgroundProcessStatus(
				this.requestId, BackgroundProcess.Status.FAILED,
				ExceptionUtil.generateStackTrace(exception))
					.invoke(this::_tellError)
					.invoke(this::_tellFinished)
			);

			return TypedActor.Stay();
		}
		else if (message instanceof TenantMessage.Stop) {
			LOGGER.info("Tenant finished " + this.requestId);
			return TypedActor.Die();
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
							return backgroundProcessService.updateBackgroundProcessStatus(
								this.requestId, BackgroundProcess.Status.FINISHED,
								"", tenant)
								.invoke(this::_tellFinished);
						}

						return backgroundProcessService.updateBackgroundProcessStatus(
							this.requestId, BackgroundProcess.Status.FAILED,
							ExceptionUtil.generateStackTrace(t))
							.invoke(() -> self.tell(new TenantMessage.Error(t)));

					})
			);

		}

		return TypedActor.Stay();

	}

	private void _tellError() {
		keycloak.tell(new KeycloakMessage.Rollback());
		schema.tell(new SchemaMessage.Rollback());
	}

	private void _tellFinished() {
		keycloak.tell(new KeycloakMessage.Stop());
		schema.tell(new SchemaMessage.Stop());
		self.tell(new TenantMessage.Stop());
	}

	private final UUID requestId;
	private String schemaName;
	private String realmName;
	private final String virtualHost;
	private String clientId;
	private String clientSecret;
	private final TypedActor.Address<TenantMessage> self;
	private TypedActor.Address<KeycloakMessage> keycloak;
	private TypedActor.Address<SchemaMessage> schema;
	private final TenantService tenantService;
	private final BackgroundProcessService backgroundProcessService;

	private static final Logger LOGGER = Logger.getLogger(TenantBehavior.class);

}
