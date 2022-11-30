package io.openk9.tenantmanager.pipe.tenant.delete;

import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteGroupMessage;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteMessage;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.openk9.tenantmanager.util.VertxUtil;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;

import java.util.UUID;

import static io.openk9.tenantmanager.actor.TypedActor.Become;
import static io.openk9.tenantmanager.actor.TypedActor.Die;
import static io.openk9.tenantmanager.actor.TypedActor.Stay;

public class DeleteBehavior implements TypedActor.Behavior<DeleteMessage> {

	public DeleteBehavior(
		DatasourceLiquibaseService datasourceLiquibaseService,
		TenantService tenantService, Keycloak keycloak, TypedActor.Address<DeleteMessage> self) {
		this.datasourceLiquibaseService = datasourceLiquibaseService;
		this.tenantService = tenantService;
		this.keycloak = keycloak;
		this.self = self;
	}

	@Override
	public TypedActor.Effect<DeleteMessage> apply(DeleteMessage timeoutMessage) {
		
		if (timeoutMessage instanceof DeleteMessage.Start) {
			DeleteMessage.Start start = (DeleteMessage.Start) timeoutMessage;
			this.virtualHost = start.virtualHost();
			this.token = UUID.randomUUID().toString();
			this.deleteGroupActor = start.deleteGroupActor();
			LOGGER.info("for virtualHost: " + virtualHost + " token: " + token);
		}
		else if (timeoutMessage instanceof DeleteMessage.Delete) {

			DeleteMessage.Delete delete = (DeleteMessage.Delete) timeoutMessage;

			if (this.token.equals(delete.token())) {
				LOGGER.info("Start Delete tenant: " + this.virtualHost);
				VertxUtil.runOnContext(
					() -> tenantService
						.findTenantByVirtualHost(virtualHost)
						.emitOn(Infrastructure.getDefaultWorkerPool())
						.onItem()
						.ifNotNull()
						.invoke((tenant) -> datasourceLiquibaseService
							.rollbackRunLiquibaseMigration(tenant.getSchemaName())
						)
						.invoke((tenant) -> LOGGER.info("schema deleted: " + tenant.getSchemaName()))
						.invoke(tenant -> keycloak.realm(tenant.getRealmName()).remove())
						.invoke((tenant) -> LOGGER.info("realm deleted: " + tenant.getRealmName()))
						.invoke(tenant -> VertxUtil.runOnContext(() -> tenantService.deleteTenant(tenant.getId())))
						.invoke((tenant) -> LOGGER.info("tenant deleted: " + tenant.getVirtualHost()))
						.onItemOrFailure()
						.invoke((tenant, t) -> {
							if (t != null) {
								LOGGER.error(t);
							}
							self.tell(new DeleteMessage.Finished());
						})
				);
			}
			else {
				LOGGER.warn("Invalid token");
			}

		}
		else if (timeoutMessage instanceof DeleteMessage.Stop) {
			_tellStop();
			LOGGER.warn("token expired: " + token);
			return Become(nextMsg -> {
				LOGGER.warn("token expired: " + token);
				return Stay();
			});
		}
		else if (timeoutMessage instanceof DeleteMessage.Finished) {
			_tellStop();
			return Die();
		}
		
		return Stay();
		
	}

	private void _tellStop() {
		if (this.deleteGroupActor != null) {
			this.deleteGroupActor.tell(new DeleteGroupMessage.RemoveDeleteRequest(this.virtualHost));
		}
	}

	private TypedActor.Address<DeleteGroupMessage> deleteGroupActor;
	private String virtualHost;
	private String token;
	private final DatasourceLiquibaseService datasourceLiquibaseService;
	private final TenantService tenantService;
	private final Keycloak keycloak;
	private final TypedActor.Address<DeleteMessage> self;

	private static final Logger LOGGER = Logger.getLogger(
		DeleteBehavior.class);
	
}
