package io.openk9.tenantmanager.pipe.tenant.delete;

import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteGroupMessage;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteMessage;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.openk9.tenantmanager.util.UniUtil;
import io.openk9.tenantmanager.util.VertxUtil;
import io.smallrye.mutiny.Uni;
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
						.call((tenant) -> {

							Uni<Void> deleteSchema =
								UniUtil.fromRunnable(
									() -> datasourceLiquibaseService.rollbackRunLiquibaseMigration(
										tenant.getSchemaName())
									)
								.invoke(() -> LOGGER.info("schema deleted: " + tenant.getSchemaName()));

							Uni<Void> deleteKeycloak = UniUtil.fromRunnable(
								() -> keycloak.realm(tenant.getRealmName()).remove())
								.invoke(() -> LOGGER.info("keycloak deleted: " + tenant.getRealmName()));

							Uni<Void> deleteTenant =
								Uni
									.createFrom()
									.emitter(sink ->
										VertxUtil.runOnContext(() ->
											tenantService
												.deleteTenant(tenant.getId())
												.onItemOrFailure()
												.invoke((v, t) -> {
													if (t != null) {
														sink.fail(t);
													}
													else {
														sink.complete(null);
													}
												})
										)
									)
									.invoke(() -> LOGGER.info("tenant deleted: " + tenant.getId()))
									.replaceWithVoid();

							return Uni
								.join()
								.all(deleteSchema, deleteKeycloak, deleteTenant)
								.andCollectFailures()
								.onItemOrFailure()
								.invoke((__, t) -> {
									if (t != null) {
										LOGGER.error(t.getMessage(), t);
									}
									self.tell(new DeleteMessage.Finished());
								});
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
