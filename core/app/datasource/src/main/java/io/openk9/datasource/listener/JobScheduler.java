package io.openk9.datasource.listener;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import com.typesafe.akka.extension.quartz.QuartzSchedulerTypedExtension;
import com.typesafe.config.Config;
import io.openk9.common.util.VertxUtil;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.Scheduler;
import io.openk9.datasource.pipeline.SchedulationKeyUtils;
import io.openk9.datasource.pipeline.actor.MessageGateway;
import io.openk9.datasource.pipeline.actor.Schedulation;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverContext;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.util.ActorActionListener;
import io.openk9.datasource.util.CborSerializable;
import io.smallrye.mutiny.Uni;
import io.vavr.Function3;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetComposableIndexTemplateRequest;
import org.elasticsearch.client.indices.GetComposableIndexTemplatesResponse;
import org.elasticsearch.client.indices.PutComposableIndexTemplateRequest;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.cluster.metadata.Template;
import org.elasticsearch.rest.RestStatus;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import scala.Option;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class JobScheduler {

	private static final Logger log = Logger.getLogger(JobScheduler.class);
	
	public sealed interface Command extends CborSerializable {}
	public record Initialize(List<ScheduleDatasource> schedulatedJobs) implements Command {}
	public record ScheduleDatasource(
		String tenantName, long datasourceId, boolean schedulable, String cron
	) implements Command {}
	public record UnScheduleDatasource(String tenantName, long datasourceId) implements Command {}
	public record TriggerDatasource(
		String tenantName, long datasourceId, Boolean startFromFirst) implements Command {}
	public record TriggerDatasourcePurge(String tenantName, long datasourceId) implements Command {}
	private record ScheduleDatasourceInternal(
		String tenantName, long datasourceId, boolean schedulable, String cron
	) implements Command {}
	private record UnScheduleJobInternal(String jobName) implements Command {}
	private record TriggerDatasourceInternal(
		String tenantName, Datasource datasource, Boolean startFromFirst) implements Command {}
	private record InvokePluginDriverInternal(
		String tenantName, io.openk9.datasource.model.Scheduler scheduler, boolean startFromFirst
	) implements Command {}
	private record MessageGatewaySubscription(Receptionist.Listing listing) implements Command {}
	private record Start(ActorRef<MessageGateway.Command> channelManagerRef) implements Command {}
	private record StartSchedulerInternal(
		String tenantName, Datasource datasource, boolean startFromFirst) implements Command {}
	private record CopyIndexTemplate(
		String tenantName, io.openk9.datasource.model.Scheduler scheduler) implements Command {}
	private record PersistSchedulerInternal(
		String tenantName, Scheduler scheduler, Throwable throwable) implements Command {}
	private record CancelSchedulation(String tenantName, Scheduler scheduler) implements Command {}

	public static Behavior<Command> create(
		HttpPluginDriverClient httpPluginDriverClient,
		Mutiny.SessionFactory sessionFactory,
		RestHighLevelClient restHighLevelClient) {

		return Behaviors.setup(ctx -> {

			log.info("setup job-scheduler");

			QuartzSchedulerTypedExtension quartzSchedulerTypedExtension =
				QuartzSchedulerTypedExtension.get(ctx.getSystem());

			ActorRef<Receptionist.Listing> listingActorRef =
				ctx.messageAdapter(
					Receptionist.Listing.class,
					MessageGatewaySubscription::new
				);

			ctx
				.getSystem()
				.receptionist()
				.tell(Receptionist.subscribe(MessageGateway.SERVICE_KEY, listingActorRef));

			return setup(
				httpPluginDriverClient,
				sessionFactory,
				restHighLevelClient,
				ctx,
				quartzSchedulerTypedExtension,
				new ArrayDeque<>()
			);


		});
	}

	private static Behavior<Command> setup(
		HttpPluginDriverClient httpPluginDriverClient,
		Mutiny.SessionFactory sessionFactory,
		RestHighLevelClient restHighLevelClient,
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		Queue<Command> lag) {

		return Behaviors
			.receive(Command.class)
			.onMessage(MessageGatewaySubscription.class,
				mgs -> onSetupMessageGatewaySubscription(ctx, mgs))
			.onMessage(Start.class, start -> {
				Command command = lag.poll();

				while (command != null) {
					ctx.getSelf().tell(command);
					command = lag.poll();
				}

				return initial(
					ctx,
					quartzSchedulerTypedExtension,
					httpPluginDriverClient,
					sessionFactory,
					restHighLevelClient,
					start.channelManagerRef,
					new ArrayList<>()
				);
			})
			.onAnyMessage(command -> {
				ArrayDeque<Command> newLag = new ArrayDeque<>(lag);
				newLag.add(command);

				if (log.isDebugEnabled()) {
					log.debugf("there are %d commands waiting...", newLag.size());
				}

				return setup(
					httpPluginDriverClient,
					sessionFactory,
					restHighLevelClient,
					ctx,
					quartzSchedulerTypedExtension,
					newLag
				);
			})
			.build();
	}

	private static Behavior<Command> initial(
		ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		HttpPluginDriverClient httpPluginDriverClient,
		Mutiny.SessionFactory sessionFactory,
		RestHighLevelClient restHighLevelClient,
		ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		return Behaviors.receive(Command.class)
			.onMessage(Initialize.class, cmd -> onInitialize(cmd, ctx))
			.onMessage(MessageGatewaySubscription.class, mgs -> onInitialMessageGatewaySubscription(ctx, mgs, quartzSchedulerTypedExtension, httpPluginDriverClient, sessionFactory, restHighLevelClient, messageGateway, jobNames))
			.onMessage(ScheduleDatasource.class, ad -> onAddDatasource(ad, ctx))
			.onMessage(UnScheduleDatasource.class, rd -> onRemoveDatasource(rd, ctx))
			.onMessage(TriggerDatasource.class, jm -> onTriggerDatasource(jm, ctx, sessionFactory))
			.onMessage(TriggerDatasourcePurge.class, tdp -> onTriggerDatasourcePurge(tdp, ctx, restHighLevelClient, sessionFactory))
			.onMessage(ScheduleDatasourceInternal.class, sdi -> onScheduleDatasourceInternal(sdi, ctx, quartzSchedulerTypedExtension, httpPluginDriverClient, sessionFactory, restHighLevelClient, messageGateway, jobNames))
			.onMessage(UnScheduleJobInternal.class, rd -> onUnscheduleJobInternal(rd, ctx, quartzSchedulerTypedExtension, httpPluginDriverClient, sessionFactory, restHighLevelClient, messageGateway, jobNames))
			.onMessage(TriggerDatasourceInternal.class, tdi -> onTriggerDatasourceInternal(tdi, ctx, sessionFactory, messageGateway))
			.onMessage(InvokePluginDriverInternal.class, ipdi -> onInvokePluginDriverInternal(ctx, httpPluginDriverClient, ipdi.tenantName, ipdi.scheduler, ipdi.startFromFirst))
			.onMessage(StartSchedulerInternal.class, ssi -> onStartScheduler(ctx, sessionFactory, messageGateway, ssi))
			.onMessage(CopyIndexTemplate.class, cit -> onCopyIndexTemplate(ctx, restHighLevelClient, cit))
			.onMessage(PersistSchedulerInternal.class, pndi -> onPersistSchedulerInternal(ctx, sessionFactory, messageGateway, pndi))
			.onMessage(CancelSchedulation.class, cs -> onCancelSchedulation(ctx, cs))
			.build();

	}

	private static Behavior<Command> onInitialize(Initialize cmd, ActorContext<Command> ctx) {

		log.info("init job-scheduler");

		for (ScheduleDatasource scheduleDatasource : cmd.schedulatedJobs()) {
			log.infof(
				"scheduling jobs for datasource with id %d on tenant %s...",
				scheduleDatasource.datasourceId(),
				scheduleDatasource.tenantName()
			);

			ctx.getSelf().tell(scheduleDatasource);
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onUnscheduleJobInternal(
		UnScheduleJobInternal msg, ActorContext<Command> ctx,
		QuartzSchedulerTypedExtension quartzSchedulerTypedExtension,
		HttpPluginDriverClient httpPluginDriverClient, Mutiny.SessionFactory sessionFactory,
		RestHighLevelClient restHighLevelClient, ActorRef<MessageGateway.Command> messageGateway,
		List<String> jobNames) {

		String jobName = msg.jobName;

		if (jobNames.contains(jobName)) {
			quartzSchedulerTypedExtension.deleteJobSchedule(jobName);
			quartzSchedulerTypedExtension.deleteJobSchedule(jobName + "-purge");

			List<String> newJobNames = new ArrayList<>(jobNames);
			newJobNames.remove(jobName);
			log.infof("Job removed: %s", jobName);

			return initial(
				ctx, quartzSchedulerTypedExtension, httpPluginDriverClient,
				sessionFactory, restHighLevelClient, messageGateway, newJobNames);
		}

		log.infof("Job not found: %s", jobName);

		return Behaviors.same();
	}

	private static Behavior<Command> onSetupMessageGatewaySubscription(
		ActorContext<Command> ctx, MessageGatewaySubscription mgs) {

		mgs
			.listing
			.getServiceInstances(MessageGateway.SERVICE_KEY)
			.stream()
			.filter(JobScheduler::isLocalActorRef)
			.findFirst()
			.map(JobScheduler.Start::new)
			.ifPresentOrElse(
				cmd -> ctx.getSelf().tell(cmd),
				() -> log.error("ChannelManager not found"));

		return Behaviors.same();

	}

	private static Behavior<Command> onInitialMessageGatewaySubscription(
		ActorContext<Command> ctx, MessageGatewaySubscription mgs, QuartzSchedulerTypedExtension quartzSchedulerTypedExtension, HttpPluginDriverClient httpPluginDriverClient, Mutiny.SessionFactory sessionFactory, RestHighLevelClient restHighLevelClient, ActorRef<MessageGateway.Command> messageGateway, List<String> jobNames) {

		Optional<ActorRef<MessageGateway.Command>> actorRefOptional = mgs.listing
			.getServiceInstances(MessageGateway.SERVICE_KEY)
			.stream()
			.filter(JobScheduler::isLocalActorRef)
			.findFirst();

		if (actorRefOptional.isPresent()) {
			return initial(ctx, quartzSchedulerTypedExtension, httpPluginDriverClient, sessionFactory, restHighLevelClient, actorRefOptional.get(), jobNames);
		}

		return Behaviors.same();

	}

	private static Behavior<Command> onTriggerDatasourceInternal(
		TriggerDatasourceInternal triggerDatasourceInternal,
		ActorContext<Command> ctx,
		Mutiny.SessionFactory sessionFactory,
		ActorRef<MessageGateway.Command> messageGateway) {

		Datasource datasource = triggerDatasourceInternal.datasource;
		String tenantName = triggerDatasourceInternal.tenantName;
		Boolean startFromFirst = triggerDatasourceInternal.startFromFirst;

		PluginDriver pluginDriver = datasource.getPluginDriver();

		log.infof("Job executed: %s", datasource.getName());

		if (pluginDriver == null) {
			log.warnf(
				"datasource with id: %s has no pluginDriver", datasource.getId());

			return Behaviors.same();
		}

		VertxUtil.runOnContext(() ->
			sessionFactory.withStatelessTransaction(
				tenantName,
				(s, t) -> s.createQuery(
					"select s " +
						"from Scheduler s " +
						"where s.datasource.id = :datasourceId " +
						"and s.status in ('STARTED', 'ERROR')",
						Scheduler.class)
					.setParameter("datasourceId", datasource.getId())
					.getResultList()
					.flatMap(list -> {
						if (list != null && !list.isEmpty()) {

							for (Scheduler scheduler : list) {
								log.warnf(
									"A Scheduler with id %s for datasource %s is %s",
									scheduler.getId(),
									datasource.getId(),
									scheduler.getStatus()
								);
							}
							return Uni.createFrom().voidItem();
						}

						if (startFromFirst != null) {
							ctx.getSelf().tell(
								new StartSchedulerInternal(tenantName, datasource, startFromFirst));
							return Uni.createFrom().voidItem();
						}
						else {
							return isReindexRequest(s, datasource.getId())
								.flatMap(isReindex -> {
									ctx.getSelf().tell(new StartSchedulerInternal(
										tenantName, datasource, isReindex));
									return Uni.createFrom().voidItem();
								});
						}
					})
			)
		);

		return Behaviors.same();
	}

	private static Uni<Boolean> isReindexRequest(Mutiny.StatelessSession s, long datasourceId) {
		return s.createQuery(
				"select case " +
					"	when d.reindexRate > 0 then mod(count(s.id), d.reindexRate) " +
					"	else -1 " +
					"end " +
					"from Scheduler s " +
					"join s.datasource d " +
					"where d.id = :datasourceId " +
					"group by d.id, d.reindexRate", Integer.class)
			.setParameter("datasourceId", datasourceId)
			.getSingleResult()
			.map(integer -> integer.equals(0));
	}

	private static Behavior<Command> onInvokePluginDriverInternal(
		ActorContext<Command> ctx, HttpPluginDriverClient httpPluginDriverClient,
		String tenantName, Scheduler scheduler,
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
					)
					.onFailure()
					.invoke(throwable -> ctx
						.getSelf()
						.tell(new CancelSchedulation(tenantName, scheduler))
					)
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
		Mutiny.SessionFactory sessionFactory,
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

				quartzSchedulerTypedExtension.updateTypedJobSchedule(
					jobName + "-purge",
					ctx.getSelf(),
					new TriggerDatasourcePurge(tenantName, datasourceId),
					Option.empty(),
					getPurgeCron(ctx),
					Option.empty(),
					quartzSchedulerTypedExtension.defaultTimezone()
				);

				log.infof("Job updated: %s datasourceId: %s", jobName, datasourceId);

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

				quartzSchedulerTypedExtension.createTypedJobSchedule(
					jobName + "-purge",
					ctx.getSelf(),
					new TriggerDatasourcePurge(tenantName, datasourceId),
					Option.empty(),
					getPurgeCron(ctx),
					Option.empty(),
					quartzSchedulerTypedExtension.defaultTimezone()
				);


				log.infof("Job created: %s datasourceId: %s", jobName, datasourceId);

				List<String> newJobNames = new ArrayList<>(jobNames);

				newJobNames.add(jobName);

				return initial(
					ctx, quartzSchedulerTypedExtension, httpPluginDriverClient,
					sessionFactory, restHighLevelClient, messageGatewayService, newJobNames);

			}
		}
		else if (jobNames.contains(jobName)) {
			ctx.getSelf().tell(new UnScheduleDatasource(tenantName, datasourceId));
			log.infof("job is not schedulable, removing job: %s", jobName);
			return Behaviors.same();
		}

		log.infof("Job not created: datasourceId: %s, the datasource is not schedulable", datasourceId);

		return Behaviors.same();

	}

	private static Behavior<Command> onTriggerDatasource(
		TriggerDatasource jobMessage, ActorContext<Command> ctx,
		Mutiny.SessionFactory sessionFactory) {

		long datasourceId = jobMessage.datasourceId;
		String tenantName = jobMessage.tenantName;
		Boolean startFromFirst = jobMessage.startFromFirst;

		loadDatasourceAndCreateSelfMessage(
			tenantName, datasourceId, sessionFactory, ctx, startFromFirst,
			TriggerDatasourceInternal::new
		);

		return Behaviors.same();

	}

	private static Behavior<Command> onTriggerDatasourcePurge(
		TriggerDatasourcePurge tdp, ActorContext<Command> ctx,
		RestHighLevelClient esClient, Mutiny.SessionFactory sessionFactory) {

		String tenantName = tdp.tenantName();
		long datasourceId = tdp.datasourceId;

		ctx.spawnAnonymous(
			DatasourcePurge.create(tenantName, datasourceId, esClient, sessionFactory));

		return Behaviors.same();
	}

	private static Behavior<Command> onRemoveDatasource(
		UnScheduleDatasource removeDatasource, ActorContext<Command> ctx) {

		long datasourceId = removeDatasource.datasourceId;
		String tenantName = removeDatasource.tenantName;

		String jobName = tenantName + "-" + datasourceId;

		ctx.getSelf().tell(new UnScheduleJobInternal(jobName));

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
		Mutiny.SessionFactory sessionFactory,
		ActorContext<Command> ctx, Boolean startFromFirst,
		Function3<String, Datasource, Boolean, Command> selfMessageCreator) {
		VertxUtil.runOnContext(() ->
			sessionFactory.withStatelessTransaction(tenantName, (s, t) ->
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
									startFromFirst
								)
						)
					)
			)
		);
	}


	private static Behavior<Command> onStartScheduler(
		ActorContext<Command> ctx, Mutiny.SessionFactory sessionFactory,
		ActorRef<MessageGateway.Command> messageGateway,
		StartSchedulerInternal startSchedulerInternal) {

		String tenantName = startSchedulerInternal.tenantName;
		Datasource datasource = startSchedulerInternal.datasource;
		boolean startFromFirst = startSchedulerInternal.startFromFirst;

		io.openk9.datasource.model.Scheduler scheduler = new io.openk9.datasource.model.Scheduler();
		scheduler.setScheduleId(UUID.randomUUID().toString());
		scheduler.setDatasource(datasource);
		scheduler.setOldDataIndex(datasource.getDataIndex());
		scheduler.setStatus(io.openk9.datasource.model.Scheduler.SchedulerStatus.STARTED);

		DataIndex oldDataIndex = scheduler.getOldDataIndex();

		log.infof("A Scheduler with schedule-id %s is starting", scheduler.getScheduleId());

		if (oldDataIndex == null || startFromFirst) {

			String newDataIndexName = datasource.getId() + "-data-" + scheduler.getScheduleId();

			DataIndex newDataIndex = new DataIndex();
			newDataIndex.setName(newDataIndexName);
			newDataIndex.setDatasource(datasource);
			scheduler.setNewDataIndex(newDataIndex);

			if (oldDataIndex != null) {
				Set<DocType> docTypes = oldDataIndex.getDocTypes();
				if (docTypes != null && !docTypes.isEmpty()) {
					ctx.getSelf().tell(new CopyIndexTemplate(tenantName, scheduler));
				}
				else {
					persistScheduler(
						ctx, sessionFactory, messageGateway, tenantName, scheduler, true);
				}
			}
			else {
				persistScheduler(
					ctx, sessionFactory, messageGateway, tenantName, scheduler, true);
			}
		}
		else {
			persistScheduler(
				ctx, sessionFactory, messageGateway, tenantName, scheduler, false);
		}

		return Behaviors.same();
	}

	private static Behavior<Command> onCopyIndexTemplate(
		ActorContext<Command> ctx, RestHighLevelClient restHighLevelClient, CopyIndexTemplate cit) {
		Scheduler scheduler = cit.scheduler;
		String tenantName = cit.tenantName;

		DataIndex oldDataIndex = scheduler.getOldDataIndex();
		DataIndex newDataIndex = scheduler.getNewDataIndex();
		String newDataIndexName = newDataIndex.getName();

		IndicesClient indices = restHighLevelClient.indices();

		indices.getIndexTemplateAsync(
			new GetComposableIndexTemplateRequest(oldDataIndex.getName() + "-template"),
			RequestOptions.DEFAULT, new ActionListener<>() {
				@Override
				public void onResponse(GetComposableIndexTemplatesResponse indexTemplate) {
					for (ComposableIndexTemplate composableIndexTemplate : indexTemplate.getIndexTemplates().values()) {

						PutComposableIndexTemplateRequest request =
							new PutComposableIndexTemplateRequest();

						Template template = composableIndexTemplate.template();

						ComposableIndexTemplate newComposableIndexTemplate =
							new ComposableIndexTemplate(
								List.of(newDataIndexName),
								new Template(
									template.settings(),
									template.mappings(),
									template.aliases()
								),
								composableIndexTemplate.composedOf(),
								composableIndexTemplate.priority(),
								composableIndexTemplate.version(),
								composableIndexTemplate.metadata()
							);

						request
							.name(newDataIndexName + "-template")
							.indexTemplate(newComposableIndexTemplate);

						indices.putIndexTemplateAsync(
							request,
							RequestOptions.DEFAULT,
							ActorActionListener.of(ctx.getSelf(), (r, t) ->
								new PersistSchedulerInternal(tenantName, scheduler, t))
						);
					}
				}

				@Override
				public void onFailure(Exception e) {
					if (e instanceof ElasticsearchStatusException
						&& ((ElasticsearchStatusException)e).status() == RestStatus.NOT_FOUND) {
						log.warn("Cannot Copy Index Template", e);
						ctx.getSelf().tell(
							new PersistSchedulerInternal(tenantName, scheduler, null));
					}
					else  {
						ctx.getSelf().tell(
							new PersistSchedulerInternal(null, null, e));
					}
				}
			});

		return Behaviors.same();
	}

	private static Behavior<Command> onPersistSchedulerInternal(
		ActorContext<Command> ctx, Mutiny.SessionFactory sessionFactory,
		ActorRef<MessageGateway.Command> messageGateway, PersistSchedulerInternal pndi) {

		String tenantName = pndi.tenantName;
		Scheduler scheduler = pndi.scheduler;
		Throwable t = pndi.throwable;

		if (t != null) {
			log.error("cannot create index-template", t);
			return Behaviors.same();
		}

		persistScheduler(ctx, sessionFactory, messageGateway, tenantName, scheduler, true);

		return Behaviors.same();
	}

	private static void persistScheduler(
		ActorContext<Command> ctx, Mutiny.SessionFactory sessionFactory,
		ActorRef<MessageGateway.Command> messageGateway, String tenantName, Scheduler scheduler,
		boolean startFromFirst) {

		VertxUtil.runOnContext(() ->
			sessionFactory
				.withTransaction(tenantName, (s, t) ->  {
					DataIndex oldDataIndex = scheduler.getOldDataIndex();
					DataIndex newDataIndex = scheduler.getNewDataIndex();

					if (oldDataIndex != null && newDataIndex != null) {
						Set<DocType> docTypes = oldDataIndex.getDocTypes();
						if (docTypes != null && !docTypes.isEmpty()) {
							Set<DocType> refreshed = new LinkedHashSet<>();

							for (DocType docType : docTypes) {
								refreshed.add(s.getReference(docType));
							}
							newDataIndex.setDocTypes(refreshed);
						}
					}

					return s
							.persist(scheduler)
							.invoke(() -> {
								messageGateway.tell(new MessageGateway.Register(
									SchedulationKeyUtils.getValue(
										tenantName, scheduler.getScheduleId())));
								ctx
									.getSelf()
									.tell(new InvokePluginDriverInternal(
										tenantName, scheduler, startFromFirst));
							});
					}
				)
		);
	}

	private static Behavior<Command> onCancelSchedulation(
		ActorContext<Command> ctx, CancelSchedulation cs) {

		String tenantName = cs.tenantName;
		Scheduler scheduler = cs.scheduler;
		String scheduleId = scheduler.getScheduleId();

		ClusterSharding clusterSharding = ClusterSharding.get(ctx.getSystem());

		EntityRef<Schedulation.Command> schedulationRef = clusterSharding.entityRefFor(
			Schedulation.ENTITY_TYPE_KEY, SchedulationKeyUtils.getValue(tenantName, scheduleId));

		schedulationRef.tell(Schedulation.Cancel.INSTANCE);

		return Behaviors.same();
	}

	private static <T> boolean isLocalActorRef(ActorRef<T> actorRef) {
		return actorRef.path().address().port().isEmpty();
	}

	private static String getPurgeCron(ActorContext<?> context) {
		Config config = context.getSystem().settings().config();

		String configPath = "io.openk9.schedulation.purge.cron";

		if (config.hasPathOrNull(configPath)) {
			if (config.getIsNull(configPath)) {
				return EVERY_DAY_AT_1_AM;
			} else {
				return config.getString(configPath);
			}
		} else {
			return EVERY_DAY_AT_1_AM;
		}

	}

	private final static String EVERY_DAY_AT_1_AM = "0 0 1 * * ?";

}
