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

@ApplicationScoped
public class SchedulerInitializerActor {

	public Uni<Void> initJobScheduler(List<JobScheduler.ScheduleDatasource> schedulatedJobs) {

		return getSchedulerRef()
			.invoke(ref -> {
				log.infof("defining schedulation for %d datasources", schedulatedJobs.size());
				ref.tell(new JobScheduler.Initialize(schedulatedJobs));
			})
			.replaceWithVoid();
	}

	public Uni<Void> scheduleDataSource(
		String tenantName, long datasourceId, boolean schedulable, String cron) {

		return getSchedulerRef()
			.invoke(ref -> ref.tell(new JobScheduler.ScheduleDatasource(
				tenantName, datasourceId, schedulable, cron)))
			.replaceWithVoid();
	}

	public Uni<Void> unScheduleDataSource(String tenantName, long datasourceId) {
		return getSchedulerRef()
			.invoke(ref -> ref.tell(
				new JobScheduler.UnScheduleDatasource(tenantName, datasourceId)))
			.replaceWithVoid();
	}

	public Uni<Void> triggerDataSource(
		String tenantName, long datasourceId, Boolean startFromFirst) {
		return getSchedulerRef()
			.invoke(ref -> ref.tell(new JobScheduler.TriggerDatasource(
				tenantName, datasourceId, startFromFirst)))
			.replaceWithVoid();
	}

	private Uni<ActorRef<JobScheduler.Command>> getSchedulerRef() {

		return vertx.executeBlocking(
			Uni.createFrom().emitter((emitter) -> {
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
					emitter.complete(actorRef);
				}
				catch (Exception e) {
					log.error("error getting job-scheduler", e);
					emitter.fail(e);
				}
			})
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
