package io.openk9.datasource.pipeline;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.events.DatasourceEventBus;
import io.openk9.datasource.events.DatasourceMessage;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.actor.Schedulation;
import io.openk9.datasource.service.SchedulerService;

import javax.enterprise.inject.spi.CDI;

public class NotificationSender extends AbstractBehavior<NotificationSender.Command> {

	private final SchedulerService service;
	private final DatasourceEventBus sender;
	private final Schedulation.SchedulationKey schedulationKey;
	private final Scheduler scheduler;

	public NotificationSender(
		ActorContext<Command> context,
		Scheduler scheduler,
		Schedulation.SchedulationKey schedulationKey) {
		super(context);
		this.service = CDI.current().select(SchedulerService.class).get();
		this.sender = CDI.current().select(DatasourceEventBus.class).get();
		this.schedulationKey = schedulationKey;
		this.scheduler = scheduler;
		context.getSelf().tell(Start.INSTANCE);
	}

	public static Behavior<Command> create(
		Scheduler scheduler, Schedulation.SchedulationKey key) {
		return Behaviors.setup(ctx -> new NotificationSender(ctx, scheduler, key));
	}

	@Override
	public Receive<Command> createReceive() {
		return newReceiveBuilder()
			.onMessageEquals(Start.INSTANCE, this::onStart)
			.build();
	}

	private Behavior<Command> onStart() {

		String tenantName = schedulationKey.tenantId();
		String scheduleId = schedulationKey.scheduleId();

		getContext()
			.getLog()
			.info(
				"Sending notification for tenant {} scheduleId {} for deleted content",
				tenantName, scheduleId
			);

		VertxUtil.runOnContext(() -> service.getDiff(tenantName, scheduleId), list -> {

			Long datasourceId = scheduler.getDatasource().getId();
			DataIndex newDataIndex = scheduler.getNewDataIndex();

			for (String deletedContentId : list) {
				sender.sendEvent(
					DatasourceMessage
						.Delete
						.builder()
						.indexName(newDataIndex.getName())
						.datasourceId(datasourceId)
						.tenantId(tenantName)
						.contentId(deletedContentId)
						.build()
				);
			}

		});

		return Behaviors.stopped();
	}

	public sealed interface Command {}
	private enum Start implements Command {INSTANCE}

}
