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

	private List<JobScheduler.ScheduleDatasource> schedulatedJobs;

	public Uni<Void> initJobScheduler(List<JobScheduler.ScheduleDatasource> schedulatedJobs) {
		this.schedulatedJobs = schedulatedJobs;
		return getScheduleRef(() -> null);
	}

	public Uni<Void> scheduleDataSource(
		String tenantName, long datasourceId, boolean schedulable, String cron) {

		return getScheduleRef(() ->
			new JobScheduler.ScheduleDatasource(tenantName, datasourceId, schedulable, cron));
	}

	public Uni<Void> unScheduleDataSource(String tenantName, long datasourceId) {
		return getScheduleRef(() ->
			new JobScheduler.UnScheduleDatasource(tenantName, datasourceId));
	}

	public Uni<Void> triggerDataSource(
		String tenantName, long datasourceId, Boolean startFromFirst) {
		return getScheduleRef(() ->
			new JobScheduler.TriggerDatasource(tenantName, datasourceId, startFromFirst));
	}

	private Uni<Void> getScheduleRef(Supplier<JobScheduler.Command> commandSupplier) {

		io.vertx.core.Vertx delegate = vertx.getDelegate();

		return Uni.createFrom().completionStage(delegate
			.<Void>executeBlocking(event -> {
				try {
					ActorRef<JobScheduler.Command> actorRef = ClusterSingleton
						.get(actorSystemProvider.getActorSystem())
						.init(
							SingletonActor.of(
								JobScheduler.create(
									httpPluginDriverClient,
									sessionFactory,
									restHighLevelClient,
									schedulatedJobs
								),
								"job-scheduler"
							)
						);
					JobScheduler.Command command = commandSupplier.get();
					if (command != null) {
						actorRef.tell(command);
					}
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
