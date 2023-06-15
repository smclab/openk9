package io.openk9.datasource.pipeline;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.Datasource;
import io.openk9.datasource.sql.TransactionInvoker;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SchedulerManager extends AbstractBehavior<SchedulerManager.Command> {


	public sealed interface Command {}
	private enum Tick implements Command {INSTANCE}
	public record Ping(String scheduleId, String tenantId) implements Command {}
	public record LastMessage(String scheduleId, String tenantId) implements Command {}

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
			.onMessageEquals(Tick.INSTANCE, this::onTick)
			.build();
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
		LocalDateTime value = scheduleTimers.remove(new SchedulerKey(lastMessage.scheduleId, lastMessage.tenantId));

		if (value != null) {
			VertxUtil.runOnContext(() -> transactionInvoker
				.withStatelessTransaction(lastMessage.tenantId(), s -> s
					.createQuery("select s " +
						"from Scheduler s " +
						"join fetch s.datasource " +
						"join fetch s.newDataIndex  " +
						"where s.scheduleId = :scheduleId", Scheduler.class)
					.setParameter("scheduleId", lastMessage.scheduleId())
					.getSingleResult()
					.invoke(scheduler -> datasourceActorRef.tell(
						new Datasource.SetDataIndex(
							lastMessage.tenantId(),
							scheduler.getDatasource().getId(),
							scheduler.getNewDataIndex().getId())))
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
