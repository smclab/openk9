package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.util.CborSerializable;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.inject.spi.CDI;

public class EnrichItemActor {
	public sealed interface Command extends CborSerializable {}
	public record EnrichItemCallback(
		long enrichItemId,
		String tenantId,
		ActorRef<EnrichItemCallbackResponse> replyTo
	) implements Command {}
	public sealed interface Response extends CborSerializable {}
	public record EnrichItemCallbackResponse(EnrichItem enrichItem) implements Response {}

	public static Behavior<Command> create() {

		Mutiny.SessionFactory sessionFactory =
			CDI.current().select(Mutiny.SessionFactory.class).get();
		return Behaviors.setup(ctx -> initial(ctx, sessionFactory));
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx, Mutiny.SessionFactory sessionFactory) {

		return Behaviors
			.receive(Command.class)
			.onMessage(EnrichItemCallback.class, enrichItemCallback -> {

				String tenantId = enrichItemCallback.tenantId;
				long enrichItemId = enrichItemCallback.enrichItemId;
				ActorRef<EnrichItemCallbackResponse> replyTo = enrichItemCallback.replyTo;

				VertxUtil.runOnContext(
					() -> sessionFactory.withStatelessTransaction(
						tenantId, (s, t) -> s.get(EnrichItem.class, enrichItemId)),
					ei -> replyTo.tell(new EnrichItemCallbackResponse(ei))
				);

				return Behaviors.same();

			})
			.build();
	}
}
