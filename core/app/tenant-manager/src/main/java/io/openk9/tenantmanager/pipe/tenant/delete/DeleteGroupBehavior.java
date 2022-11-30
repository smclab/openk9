package io.openk9.tenantmanager.pipe.tenant.delete;

import io.openk9.tenantmanager.actor.TypedActor;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteGroupMessage;
import io.openk9.tenantmanager.pipe.tenant.delete.message.DeleteMessage;
import io.openk9.tenantmanager.pipe.tenant.delete.message.TimeoutMessage;
import io.openk9.tenantmanager.service.DatasourceLiquibaseService;
import io.openk9.tenantmanager.service.TenantService;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.openk9.tenantmanager.actor.TypedActor.Stay;

public class DeleteGroupBehavior implements TypedActor.Behavior<DeleteGroupMessage> {

	public DeleteGroupBehavior(
		TypedActor.Address<DeleteGroupMessage> self,
		TypedActor.System system,
		DatasourceLiquibaseService datasourceLiquibaseService,
		TenantService tenantService, Keycloak keycloak) {
		this.self = self;
		this.system = system;
		this.datasourceLiquibaseService = datasourceLiquibaseService;
		this.tenantService = tenantService;
		this.keycloak = keycloak;
	}

	@Override
	public TypedActor.Effect<DeleteGroupMessage> apply(DeleteGroupMessage timeoutMessage) {

		if (timeoutMessage instanceof DeleteGroupMessage.addDeleteRequest) {

			DeleteGroupMessage.addDeleteRequest newDeleteRequest =
				(DeleteGroupMessage.addDeleteRequest) timeoutMessage;

			String virtualHost = newDeleteRequest.virtualHost();

			deleteActorMap.compute(virtualHost, (k, v) -> {

				if (v != null) {
					v.tell(new DeleteMessage.Stop());
				}

				TypedActor.Address<TimeoutMessage> timeoutDeleteActor =
					system.actorOf(TimeoutStopDeleteBehavior::new);

				TypedActor.Address<DeleteMessage> deleteActor =
					system.actorOf(self -> new DeleteBehavior(
						datasourceLiquibaseService, tenantService, keycloak,
						self));

				deleteActor.tell(new DeleteMessage.Start(self, virtualHost));

				timeoutDeleteActor.tell(
					new TimeoutMessage.Start(
						deleteActor, Duration.ofSeconds(90)));

				return deleteActor;

			});

		}
		else if (timeoutMessage instanceof DeleteGroupMessage.TellDelete) {
			DeleteGroupMessage.TellDelete tellDelete =
				(DeleteGroupMessage.TellDelete) timeoutMessage;
			if (deleteActorMap.containsKey(tellDelete.virtualHost())) {
				deleteActorMap
					.get(tellDelete.virtualHost())
					.tell(new DeleteMessage.Delete(tellDelete.token()));
			}
			else {
				LOGGER.warn("virtualHost not found: " + tellDelete.virtualHost());
			}
		}
		else if (timeoutMessage instanceof DeleteGroupMessage.RemoveDeleteRequest) {

			DeleteGroupMessage.RemoveDeleteRequest removeDeleteRequest =
				(DeleteGroupMessage.RemoveDeleteRequest) timeoutMessage;

			deleteActorMap.remove(removeDeleteRequest.virtualHost());

		}

		return Stay();

	}

	private final Map<String, TypedActor.Address<DeleteMessage>> deleteActorMap =
		new LinkedHashMap<>();

	private final TypedActor.Address<DeleteGroupMessage> self;
	private final TypedActor.System system;
	private final DatasourceLiquibaseService datasourceLiquibaseService;
	private final TenantService tenantService;
	private final Keycloak keycloak;

	private static final Logger LOGGER = Logger.getLogger(DeleteGroupBehavior.class);
	
}
