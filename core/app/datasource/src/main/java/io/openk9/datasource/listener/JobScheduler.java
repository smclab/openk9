package io.openk9.datasource.listener;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.internal.receptionist.ReceptionistMessages;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import com.typesafe.akka.extension.quartz.QuartzSchedulerTypedExtension;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.pipeline.actor.MessageGateway;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverContext;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.util.ActorActionListener;
import io.openk9.datasource.util.CborSerializable;
import io.vavr.Function3;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexTemplatesRequest;
import org.elasticsearch.client.indices.GetIndexTemplatesResponse;
import org.elasticsearch.client.indices.IndexTemplateMetadata;
import org.elasticsearch.client.indices.PutComposableIndexTemplateRequest;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.cluster.metadata.Template;
import org.elasticsearch.common.compress.CompressedXContent;
import scala.Option;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class JobScheduler {

	public sealed interface Command extends CborSerializable {}
	public record ScheduleDatasource(String tenantName, long datasourceId, boolean schedulable, String cron) implements Command {}
	public record UnScheduleDatasource(String tenantName, long datasourceId) implements Command {}
	public record TriggerDatasource(
		String tenantName, long datasourceId, Boolean startFromFirst) implements Command {}
	private record ScheduleDatasourceInternal(String tenantName, long datasourceId, boolean schedulable, String cron) implements Command {}
	private record TriggerDatasourceInternal(String tenantName, Datasource datasource, boolean startFromFirst) implements Command {}
	private record InvokePluginDriverInternal(
		String tenantName, io.openk9.datasource.model.Scheduler scheduler,
		boolean startFromFirst) implements Command {}
	private record ChannelManagerSubscribeResponse(Receptionist.Listing listing) implements Command {}
	private record Start(ActorRef<MessageGateway.Command> channelManagerRef) implements Command {}
	private record CopyIndexTemplate(String tenantName, io.openk9.datasource.model.Scheduler scheduler) implements Command {}
	private record PersistSchedulerInternal(String tenantName, Scheduler scheduler) implements Command {}


	public static Behavior<Command> create(
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker,
		RestHighLevelClient restHighLevelClient) {

		return Behaviors.setup(ctx -> {

			QuartzSchedulerTypedExtension quartzSchedulerTypedExtension =
				QuartzSchedulerTypedExtension.get(ctx.getSystem());

			ActorRef<Receptionist.Listing> listingActorRef =
				ctx.messageAdapter(
					Receptionist.Listing.class,
					JobScheduler.ChannelManagerSubscribeResponse::new);

			ctx
				.getSystem()
				.receptionist()
				.tell(new ReceptionistMessages.Subscribe<>(MessageGateway.SERVICE_KEY, listingActorRef));

			return start(
				httpPluginDriverClient, transactionInvoker, restHighLevelClient,
				ctx, quartzSchedulerTypedExtension);


		});
	}

	private static Behavior<Command> start(
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker, RestHighLevelClient restHighLevelClient,
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension) {

		return Behaviors
			.receive(Command.class)
			.onMessage(Start.class, start ->
				initial(
					ctx, quartzSchedulerTypedExtension, httpPluginDriverClient,
					transactionInvoker, restHighLevelClient,
					start.channelManagerRef,
					new ArrayList<>()
				)
			)
			.onMessage(
				ChannelManagerSubscribeResponse.class,
				cmsr -> onChannelManagerSubscribeResponse(ctx, cmsr))
			.build();
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker,
		RestHighLevelClient restHighLevelClient,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		return Behaviors.receive(Command.class)
			.onMessage(ScheduleDatasource.class, ad -> onAddDatasource(ad, ctx))
			.onMessage(UnScheduleDatasource.class, rd -> onRemoveDatasource(rd, ctx, quartzSchedulerTypedExtension, httpPluginDriverClient, transactionInvoker, restHighLevelClient, messageGateway, jobNames))
			.onMessage(TriggerDatasource.class, jm -> onTriggerDatasource(jm, ctx, transactionInvoker))
			.onMessage(ScheduleDatasourceInternal.class, sdi -> onScheduleDatasourceInternal(sdi, ctx, quartzSchedulerTypedExtension, httpPluginDriverClient, transactionInvoker, restHighLevelClient, messageGateway, jobNames))
			.onMessage(TriggerDatasourceInternal.class, tdi -> onTriggerDatasourceInternal(tdi, ctx, transactionInvoker, messageGateway))
			.onMessage(InvokePluginDriverInternal.class, ipdi -> onInvokePluginDriverInternal(httpPluginDriverClient, ipdi.tenantName, ipdi.scheduler, ipdi.startFromFirst))
			.onMessage(CopyIndexTemplate.class, cit -> onCopyIndexTemplate(ctx, restHighLevelClient, cit))
			.onMessage(PersistSchedulerInternal.class, pndi -> onPersistSchedulerInternal(ctx, transactionInvoker, messageGateway, pndi))
			.build();

	}

	private static Behavior<Command> onChannelManagerSubscribeResponse(
		ActorContext<Command> ctx, ChannelManagerSubscribeResponse cmsr) {

		cmsr
			.listing
			.getServiceInstances(MessageGateway.SERVICE_KEY)
			.stream()
			.findFirst()
			.map(JobScheduler.Start::new)
			.ifPresentOrElse(
				cmd -> ctx.getSelf().tell(cmd),
				() -> ctx.getLog().error("ChannelManager not found"));

		return Behaviors.same();

	}

	private static Behavior<Command> onTriggerDatasourceInternal(
		TriggerDatasourceInternal triggerDatasourceInternal,
		ActorContext<Command> ctx,
		TransactionInvoker transactionInvoker,
		ActorRef<MessageGateway.Command> messageGateway) {

		Datasource datasource = triggerDatasourceInternal.datasource;
		String tenantName = triggerDatasourceInternal.tenantName;
		boolean startFromFirst = triggerDatasourceInternal.startFromFirst;

		PluginDriver pluginDriver = datasource.getPluginDriver();

		ctx.getLog().info("Job executed: {}", datasource.getName());

		if (pluginDriver == null) {
			ctx.getLog().warn(
				"datasource with id: {} has no pluginDriver", datasource.getId());

			return Behaviors.same();
		}

		startScheduler(
			ctx, datasource, startFromFirst, tenantName, transactionInvoker, messageGateway);

		return Behaviors.same();
	}

	private static Behavior<Command> onInvokePluginDriverInternal(
		HttpPluginDriverClient httpPluginDriverClient,
		String tenantName, io.openk9.datasource.model.Scheduler scheduler,
		boolean startFromFirst) {

		Datasource datasource = scheduler.getDatasource();
		PluginDriver pluginDriver = datasource.getPluginDriver();

		OffsetDateTime lastIngestionDate;

		if (startFromFirst || datasource.getLastIngestionDate() == null) {
			lastIngestionDate = OffsetDateTime.ofInstant(
				Instant.ofEpochMilli(0), ZoneId.systemDefault());
		}
		else {
			lastIngestionDate = datasource.getLastIngestionDate();
		}

		switch (pluginDriver.getType()) {
			case HTTP: {
				VertxUtil.runOnContext(
					() -> httpPluginDriverClient.invoke(
						Json.decodeValue(
							pluginDriver.getJsonConfig(),
							HttpPluginDriverInfo.class),
						HttpPluginDriverContext
							.builder()
							.timestamp(lastIngestionDate)
							.tenantId(tenantName)
							.datasourceId(datasource.getId())
							.scheduleId(scheduler.getScheduleId())
							.datasourceConfig(new JsonObject(datasource.getJsonConfig()).getMap())
							.build()
					), (ignore) -> {}
				);
			}
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onScheduleDatasourceInternal(
		ScheduleDatasourceInternal scheduleDatasourceInternal,
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker,
		RestHighLevelClient restHighLevelClient,
		ActorRef<MessageGateway.Command> messageGatewayService, List<String> jobNames) {

		String tenantName = scheduleDatasourceInternal.tenantName();
		long datasourceId = scheduleDatasourceInternal.datasourceId();
		String cron = scheduleDatasourceInternal.cron();
		boolean schedulable = scheduleDatasourceInternal.schedulable();

		String jobName = tenantName + "-" + datasourceId;

		if (schedulable) {

			if (jobNames.contains(jobName)) {

				quartzSchedulerTypedExtension.updateTypedJobSchedule(
					jobName,
					ctx.getSelf(),
					new TriggerDatasource(tenantName, datasourceId, null),
					Option.empty(),
					cron,
					Option.empty(),
					quartzSchedulerTypedExtension.defaultTimezone()
				);

				ctx.getLog().info("Job updated: {} datasourceId: {}", jobName, datasourceId);

				return Behaviors.same();
			}
			else {

				quartzSchedulerTypedExtension.createTypedJobSchedule(
					jobName,
					ctx.getSelf(),
					new TriggerDatasource(tenantName, datasourceId, null),
					Option.empty(),
					cron,
					Option.empty(),
					quartzSchedulerTypedExtension.defaultTimezone()
				);

				ctx.getLog().info("Job created: {} datasourceId: {}", jobName, datasourceId);

				List<String> newJobNames = new ArrayList<>(jobNames);

				newJobNames.add(jobName);

				return initial(
					ctx, quartzSchedulerTypedExtension, httpPluginDriverClient,
					transactionInvoker, restHighLevelClient, messageGatewayService, newJobNames);

			}
		}
		else if (jobNames.contains(jobName)) {
			ctx.getSelf().tell(new UnScheduleDatasource(tenantName, datasourceId));
			ctx.getLog().info("job is not schedulable, removing job: {}", jobName);
			return Behaviors.same();
		}

		ctx.getLog().info("Job not created: datasourceId: {}, the datasource is not schedulable", datasourceId);

		return Behaviors.same();

	}

	private static Behavior<Command> onTriggerDatasource(
		TriggerDatasource jobMessage, ActorContext<Command> ctx,
		TransactionInvoker transactionInvoker) {

		long datasourceId = jobMessage.datasourceId;
		String tenantName = jobMessage.tenantName;
		Boolean startFromFirst = jobMessage.startFromFirst;

		loadDatasourceAndCreateSelfMessage(
			tenantName, datasourceId, transactionInvoker, ctx, startFromFirst,
			TriggerDatasourceInternal::new
		);

		return Behaviors.same();

	}

	private static Behavior<Command> onRemoveDatasource(
		UnScheduleDatasource removeDatasource, ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		HttpPluginDriverClient httpPluginDriverClient,
		TransactionInvoker transactionInvoker,
		RestHighLevelClient restHighLevelClient,
		ActorRef<MessageGateway.Command> messageGatewayService, List<String> jobNames) {

		long datasourceId = removeDatasource.datasourceId;
		String tenantName = removeDatasource.tenantName;

		String jobName = tenantName + "-" + datasourceId;

		if (jobNames.contains(jobName)) {
			quartzSchedulerTypedExtension.deleteJobSchedule(jobName);
			List<String> newJobNames = new ArrayList<>(jobNames);
			newJobNames.remove(jobName);
			ctx.getLog().info("Job removed: {}", jobName);
			return initial(
				ctx, quartzSchedulerTypedExtension, httpPluginDriverClient,
				transactionInvoker, restHighLevelClient, messageGatewayService, newJobNames);
		}

		ctx.getLog().info("Job not found: {}", jobName);

		return Behaviors.same();

	}

	private static Behavior<Command> onAddDatasource(
		ScheduleDatasource addDatasource, ActorContext<Command> ctx) {

		long datasourceId = addDatasource.datasourceId;
		String tenantName = addDatasource.tenantName;

		ctx.getSelf().tell(
			new ScheduleDatasourceInternal(
				tenantName, datasourceId, addDatasource.schedulable,
				addDatasource.cron
			)
		);

		return Behaviors.same();

	}

	private static void loadDatasourceAndCreateSelfMessage(
		String tenantName, long datasourceId,
		TransactionInvoker transactionInvoker,
		ActorContext<Command> ctx, Boolean startFromFirst,
		Function3<String, Datasource, Boolean, Command> selfMessageCreator) {
		VertxUtil.runOnContext(() ->
			transactionInvoker.withStatelessTransaction(tenantName, s ->
				s.createQuery(
						"select d " +
						"from Datasource d " +
						"join fetch d.pluginDriver " +
						"left join fetch d.dataIndex di " +
						"left join fetch di.docTypes " +
						"where d.id = :id", Datasource.class)
					.setParameter("id", datasourceId)
					.getSingleResult()
					.invoke(d -> ctx
						.getSelf()
						.tell(
							selfMessageCreator
								.apply(
									tenantName,
									d,
									startFromFirst == null
										? d.getReindex()
										: startFromFirst
								)
						)
					)
			)
		);
	}


	private static void startScheduler(
		ActorContext<Command> ctx, Datasource datasource,
		boolean startFromFirst, String tenantName, TransactionInvoker transactionInvoker,
		ActorRef<MessageGateway.Command> messageGateway) {


		io.openk9.datasource.model.Scheduler scheduler = new io.openk9.datasource.model.Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(datasource.getDataIndex());
		scheduler.setStatus(io.openk9.datasource.model.Scheduler.SchedulerStatus.STARTED);

		DataIndex oldDataIndex = scheduler.getOldDataIndex();

		if (oldDataIndex == null || startFromFirst) {

			String newDataIndexName = datasource.getId() + "-data-" + scheduler.getScheduleId();

			DataIndex newDataIndex = new DataIndex();
			newDataIndex.setName(newDataIndexName);
			newDataIndex.setDatasource(datasource);
			scheduler.setNewDataIndex(newDataIndex);

			if (oldDataIndex != null) {
				Set<DocType> docTypes = oldDataIndex.getDocTypes();
				if (docTypes != null && !docTypes.isEmpty()) {
					newDataIndex.setDocTypes(new LinkedHashSet<>(docTypes));
					ctx.getSelf().tell(new CopyIndexTemplate(tenantName, scheduler));
				}
				else {
					persistScheduler(ctx, transactionInvoker, messageGateway, tenantName, scheduler, true);
				}
			}

		}
		else {
			persistScheduler(ctx, transactionInvoker, messageGateway, tenantName, scheduler, false);
		}

	}

	private static Behavior<Command> onCopyIndexTemplate(
		ActorContext<Command> ctx, RestHighLevelClient restHighLevelClient, CopyIndexTemplate cit) {
		Scheduler scheduler = cit.scheduler;
		String tenantName = cit.tenantName;

		DataIndex oldDataIndex = scheduler.getOldDataIndex();
		DataIndex newDataIndex = scheduler.getNewDataIndex();
		String newDataIndexName = newDataIndex.getName();

		IndicesClient indices = restHighLevelClient.indices();

		try {
			GetIndexTemplatesResponse indexTemplate = indices.getIndexTemplate(
				new GetIndexTemplatesRequest(oldDataIndex.getName() + "-template"),
				RequestOptions.DEFAULT);

			for (IndexTemplateMetadata template : indexTemplate.getIndexTemplates()) {
				PutComposableIndexTemplateRequest request =
					new PutComposableIndexTemplateRequest();

				ComposableIndexTemplate composableIndexTemplate = new ComposableIndexTemplate(
						List.of(newDataIndexName),
						new Template(
							template.settings(),
							new CompressedXContent(Json.encode(template.mappings().sourceAsMap())),
							null),
						null, null, null, null);

				request.indexTemplate(composableIndexTemplate);

				indices.putIndexTemplateAsync(
					request,
					RequestOptions.DEFAULT,
					ActorActionListener.of(ctx.getSelf(), (r, t) -> new PersistSchedulerInternal(tenantName, scheduler))
				);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}



		return Behaviors.same();
	}

	private static Behavior<Command> onPersistSchedulerInternal(
		ActorContext<Command> ctx, TransactionInvoker transactionInvoker,
		ActorRef<MessageGateway.Command> messageGateway, PersistSchedulerInternal pndi) {

		String tenantName = pndi.tenantName;
		Scheduler scheduler = pndi.scheduler;

		persistScheduler(ctx, transactionInvoker, messageGateway, tenantName, scheduler, true);

		return Behaviors.same();
	}

	private static void persistScheduler(
		ActorContext<Command> ctx, TransactionInvoker transactionInvoker,
		ActorRef<MessageGateway.Command> messageGateway, String tenantName, Scheduler scheduler,
		boolean startFromFirst) {

		VertxUtil.runOnContext(() ->
			transactionInvoker
				.withTransaction(tenantName, (s) ->
					s
						.persist(scheduler)
						.invoke(() -> {
							messageGateway.tell(new MessageGateway.Register(
								SchedulationKeyUtils.getValue(
									tenantName, scheduler.getScheduleId())));
							ctx
								.getSelf()
								.tell(new InvokePluginDriverInternal(tenantName, scheduler, startFromFirst));
						})
				)
		);
	}

}
