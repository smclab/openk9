package io.openk9.datasource.pipeline.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.pipeline.actor.dto.EnrichItemProjection;
import io.openk9.datasource.sql.TransactionInvoker;

import javax.enterprise.inject.spi.CDI;

public class EnrichItemActor {
	public sealed interface Command {}
	public record EnrichItemCallback(long datasourceId, long enrichItemId, String tenantId, ActorRef<EnrichItemCallbackResponse> replyTo) implements Command {}
	public sealed interface Response {}
	public record EnrichItemCallbackResponse(EnrichItemProjection enrichItemProjection) implements Response {}

	public static Behavior<Command> create() {
		TransactionInvoker transactionInvoker =
			CDI.current().select(TransactionInvoker.class).get();
		return Behaviors.setup(ctx -> initial(ctx, transactionInvoker));
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx, TransactionInvoker transactionInvoker) {

		return Behaviors
			.receive(Command.class)
			.onMessage(EnrichItemCallback.class, enrichItemCallback -> {

				String tenantId = enrichItemCallback.tenantId;
				long enrichItemId = enrichItemCallback.enrichItemId;
				ActorRef<EnrichItemCallbackResponse> replyTo = enrichItemCallback.replyTo;
				long datasourceId = enrichItemCallback.datasourceId;

				VertxUtil.runOnContext(
					() -> transactionInvoker.withStatelessTransaction(
						tenantId, s ->
							s.createQuery(
								"select new io.openk9.datasource.pipeline.actor.dto.EnrichItemProjection(d.id, ei.id, ei.type, ei.serviceName, ei.jsonConfig) " +
								"from Datasource d " +
								"join d.enrichPipeline ep " +
								"join ep.enrichPipelineItems epi " +
								"join epi.enrichItem ei " +
								"where d.id = :datasourceId and ei.id = :enrichItemId",
									EnrichItemProjection.class
								)
								.setParameter("datasourceId", datasourceId)
								.setParameter("enrichItemId", enrichItemId)
								.getSingleResult()
					), eip -> replyTo.tell(new EnrichItemCallbackResponse(eip)));

				return Behaviors.same();

			})
			.build();
	}
}
