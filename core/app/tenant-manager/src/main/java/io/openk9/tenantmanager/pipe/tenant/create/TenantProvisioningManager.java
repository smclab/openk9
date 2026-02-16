package io.openk9.tenantmanager.pipe.tenant.create;

import io.openk9.tenantmanager.dto.TenantResponseDTO;
import io.openk9.tenantmanager.model.SecurityConfiguration;
import io.openk9.tenantmanager.service.dto.OAuth2Settings;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

public class TenantProvisioningManager
	extends AbstractBehavior<TenantProvisioningManager.Command> {

	public TenantProvisioningManager(ActorContext<Command> context) {
		super(context);
	}

	public static Behavior<Command> create() {
		return Behaviors.setup(TenantProvisioningManager::new);
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(CreateTenant.class, this::onCreateTenant)
			.onMessage(SagaResponse.class, this::onSagaResponse)
			.build();
	}

	private Behavior<Command> onCreateTenant(CreateTenant message) {

		ActorRef<TenantProvisioningSaga.Response> adapter =
			getContext().messageAdapter(
				TenantProvisioningSaga.Response.class,
				response -> new SagaResponse(
					response, message.replyTo())
			);

		getContext().spawnAnonymous(TenantProvisioningSaga.create(
				message.virtualHost(),
				message.tenantName(),
				message.settings(),
				adapter
			)
		);

		return Behaviors.same();
	}

	private Behavior<Command> onSagaResponse(SagaResponse message) {

		if (message.response() instanceof TenantProvisioningSaga.Success(
			TenantResponseDTO tenant)) {

			message.replyTo().tell(new Success(tenant));
		}
		else if (message.response() == TenantProvisioningSaga.Error.INSTANCE) {

			message.replyTo().tell(Error.INSTANCE);
		}

		return Behaviors.same();
	}

	public sealed interface Command {}

	public sealed interface Response {}

	public record CreateTenant(
		String virtualHost, String tenantName,
		SecurityConfiguration securityConfiguration,
		OAuth2Settings settings,
		ActorRef<Response> replyTo) implements Command {}

	private record SagaResponse(
		TenantProvisioningSaga.Response response,
		ActorRef<Response> replyTo) implements Command {}

	public record Success(TenantResponseDTO tenant) implements Response {}

	public enum Error implements Response { INSTANCE }

}
