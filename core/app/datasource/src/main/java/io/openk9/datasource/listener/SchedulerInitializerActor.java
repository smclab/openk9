package io.openk9.datasource.listener;

import akka.actor.typed.ActorRef;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.elasticsearch.client.RestHighLevelClient;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Supplier;

@ApplicationScoped
public class SchedulerInitializerActor {

	public Uni<Void> initJobScheduler(List<JobScheduler.ScheduleDatasource> schedulatedJobs) {

		return askScheduleRef(() -> {
			log.infof("initializing schedulation for % datasources", schedulatedJobs.size());
			return new JobScheduler.Initialize(schedulatedJobs);
		});
	}

	public Uni<Void> scheduleDataSource(
		String tenantName, long datasourceId, boolean schedulable, String cron) {

		return askScheduleRef(() ->
			new JobScheduler.ScheduleDatasource(tenantName, datasourceId, schedulable, cron));
	}

	public Uni<Void> unScheduleDataSource(String tenantName, long datasourceId) {
		return askScheduleRef(() ->
			new JobScheduler.UnScheduleDatasource(tenantName, datasourceId));
	}

	public Uni<Void> triggerDataSource(
		String tenantName, long datasourceId, Boolean startFromFirst) {
		return askScheduleRef(() ->
			new JobScheduler.TriggerDatasource(tenantName, datasourceId, startFromFirst));
	}

	private Uni<Void> askScheduleRef(Supplier<JobScheduler.Command> command) {

		io.vertx.core.Vertx delegate = vertx.getDelegate();

		return Uni.createFrom().completionStage(delegate
			.<Void>executeBlocking(event -> {
				try {
					ActorRef<JobScheduler.Command> actorRef = ClusterSingleton
						.get(actorSystemProvider.getActorSystem())
						.init(
							SingletonActor.of(
								JobScheduler.create(
									httpPluginDriverClient, sessionFactory, restHighLevelClient
								),
								"job-scheduler"
							)
						);
					actorRef.tell(command.get());
					event.complete(null);
				}
				catch (Exception e) {
					log.error("error getting job-scheduler", e);
					event.fail(e);
				}
			})
			.toCompletionStage()
		);

	}

	private static final Logger log = Logger.getLogger(SchedulerInitializerActor.class);
	
	@Inject
	HttpPluginDriverClient httpPluginDriverClient;

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	ActorSystemProvider actorSystemProvider;

	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject
	Vertx vertx;
}
