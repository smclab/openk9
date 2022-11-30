package io.openk9.tenantmanager.pipe.tenant.delete;

import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteGroupMessage;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import io.smallrye.context.api.ManagedExecutorConfig;
import io.smallrye.context.api.NamedInstance;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.keycloak.admin.client.Keycloak;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DeleteTenantActorSystem {

	@PostConstruct
	public void init() {
		system = new TypedActor.System(sharedConfiguredExecutor);
		deleteGroupActor = system.actorOf(self ->
			new DeleteGroupBehavior(
				self, system, datasourceLiquibaseService, tenantService,
				keycloak
			)
		);
	}

	public void runDelete(String virtualHost, String token) {
		deleteGroupActor.tell(new DeleteGroupMessage.TellDelete(virtualHost, token));
	}

	public void startDeleteTenant(String virtualHost) {

		deleteGroupActor.tell(
			new DeleteGroupMessage.addDeleteRequest(virtualHost));

	}

	@Inject
	@ManagedExecutorConfig
	@NamedInstance("delete-tenant-actor-executor")
	ManagedExecutor sharedConfiguredExecutor;

	@Inject
	DatasourceLiquibaseService datasourceLiquibaseService;
	@Inject
	TenantService tenantService;
	@Inject
	Keycloak keycloak;

	private TypedActor.System system;
	private TypedActor.Address<DeleteGroupMessage> deleteGroupActor;

}
