package io.openk9.datasource.pipeline;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.ScheduleId;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.model.Scheduler_;
import io.openk9.datasource.pipeline.actor.Datasource;
import io.openk9.datasource.sql.TransactionInvoker;

import javax.persistence.criteria.CriteriaUpdate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SchedulerManager extends AbstractBehavior<SchedulerManager.Command> {


	public sealed interface Command {}
	private enum Tick implements Command {INSTANCE}
	public record Ping(String scheduleId, String tenantId) implements Command {}
	public record LastMessage(String scheduleId, String tenantId) implements Command {}
	private record DatasourceResponseWrapper(Datasource.Response response, String tenantId, String scheduleId) implements Command {}
	private record Finish(String tenantId, String scheduleId) implements Command {}

	private final TransactionInvoker transactionInvoker;
	private final Duration maxAge;
	private final Map<SchedulerKey, LocalDateTime> scheduleTimers = new HashMap<>();
	private final ActorRef<Datasource.Command> datasourceActorRef;

	private record SchedulerKey(String scheduleId, String tenantId) {}
	public SchedulerManager(
		ActorContext<Command> context,
		TransactionInvoker transactionInvoker,
		Duration maxAge,
		ActorRef<Datasource.Command> datasourceActorRef) {

		super(context);
		this.transactionInvoker = transactionInvoker;
		this.maxAge = maxAge;
		this.datasourceActorRef = datasourceActorRef;
		context.scheduleOnce(Duration.ofMillis(1000), context.getSelf(), Tick.INSTANCE);
	}

	public static Behavior<Command> create(
		TransactionInvoker transactionInvoker,
		Duration maxAge, ActorRef<Datasource.Command> datasourceActorRef) {

		return Behaviors.setup(ctx ->
			new SchedulerManager(ctx, transactionInvoker, maxAge, datasourceActorRef));
	}

	public static Behavior<Command> create(
		TransactionInvoker transactionInvoker, ActorRef<Datasource.Command> datasourceActorRef) {

		return Behaviors.setup(ctx -> new SchedulerManager(
			ctx, transactionInvoker, Duration.ofSeconds(5), datasourceActorRef));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessage(Ping.class, this::onPing)
			.onMessage(LastMessage.class, this::onLastMessage)
			.onMessage(DatasourceResponseWrapper.class, this::onDatasourceResponse)
			.onMessage(Finish.class, this::onFinish)
			.onMessageEquals(Tick.INSTANCE, this::onTick)
			.build();
	}

	private Behavior<Command> onFinish(Finish f) {

		getContext().getLog().info(f.toString());

		VertxUtil.runOnContext(() -> transactionInvoker.withTransaction(
			f.tenantId,
			s -> {
				CriteriaUpdate<Scheduler> criteriaUpdate =
					transactionInvoker.getCriteriaBuilder().createCriteriaUpdate(Scheduler.class);
				criteriaUpdate.set(Scheduler_.status, Scheduler.SchedulerStatus.FINISHED);
				return s.createQuery(criteriaUpdate).executeUpdate();
			})
		);
		return Behaviors.same();
	}

	private Behavior<Command> onDatasourceResponse(DatasourceResponseWrapper drw) {
		if (drw.response == Datasource.SetDataIndexSuccess.INSTANCE) {
			getContext().getSelf().tell(new Finish(drw.tenantId, drw.scheduleId));
		}
		return Behaviors.same();
	}

	private Behavior<Command> onTick() {
		for (Map.Entry<SchedulerKey, LocalDateTime> entry : scheduleTimers.entrySet()) {
			if (Duration.between(entry.getValue(), LocalDateTime.now()).compareTo(maxAge) > 0) {
				SchedulerKey key = entry.getKey();
				getContext().getLog().info("key: {} schedule expired", key);
				getContext().getSelf().tell(new LastMessage(key.scheduleId, key.tenantId));
			}
		}
		return Behaviors.same();
	}

	private Behavior<Command> onLastMessage(LastMessage lastMessage) {
		String scheduleId = lastMessage.scheduleId;
		String tenantId = lastMessage.tenantId;

		LocalDateTime value = scheduleTimers.remove(new SchedulerKey(scheduleId, tenantId));

		ActorRef<Datasource.Response> datasourceMessageAdapter = getContext().messageAdapter(
			Datasource.Response.class,
			response -> new DatasourceResponseWrapper(response, tenantId, scheduleId));

		if (value != null) {
			VertxUtil.runOnContext(() -> transactionInvoker
				.withStatelessTransaction(lastMessage.tenantId(), s -> s
					.createQuery("select s " +
						"from Scheduler s " +
						"join fetch s.datasource " +
						"join fetch s.newDataIndex  " +
						"where s.scheduleId = :scheduleId", Scheduler.class)
					.setParameter("scheduleId", new ScheduleId(UUID.fromString(scheduleId)))
					.getSingleResult()
					.onItemOrFailure()
					.invoke((scheduler, throwable) -> {
						if (scheduler != null) {
							datasourceActorRef.tell(
								new Datasource.SetDataIndex(
									tenantId,
									scheduler.getDatasource().getId(),
									scheduler.getNewDataIndex().getId(),
									datasourceMessageAdapter));
						}
						else {
							getContext().getSelf().tell(new Finish(tenantId, scheduleId));
						}
					})
				)
			);
		}

		return Behaviors.same();
	}

	private Behavior<Command> onPing(Ping ping) {
		scheduleTimers.put(new SchedulerKey(ping.scheduleId, ping.tenantId), LocalDateTime.now());
		return Behaviors.same();
	}


}
