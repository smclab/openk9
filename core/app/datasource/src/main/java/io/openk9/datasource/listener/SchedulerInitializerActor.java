package io.openk9.datasource.listener;

import akka.actor.typed.ActorRef;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.sql.TransactionInvoker;
import org.elasticsearch.client.RestHighLevelClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SchedulerInitializerActor {

	public void scheduleDataSource(String tenantName, long datasourceId, boolean schedulable, String cron) {
		getSchedulerRef().tell(new JobScheduler.ScheduleDatasource(tenantName, datasourceId, schedulable, cron));
	}

	public void unScheduleDataSource(String tenantName, long datasourceId) {
		getSchedulerRef().tell(new JobScheduler.UnScheduleDatasource(tenantName, datasourceId));
	}

	public void triggerDataSource(
		String tenantName, long datasourceId, boolean startFromFirst) {
		getSchedulerRef().tell(new JobScheduler.TriggerDatasource(tenantName, datasourceId, startFromFirst));
	}

	private ActorRef<JobScheduler.Command> getSchedulerRef() {
		return ClusterSingleton.get(actorSystemProvider.getActorSystem())
			.init(
				SingletonActor.of(
					JobScheduler.create(
						httpPluginDriverClient, transactionInvoker, restHighLevelClient
					), "job-scheduler")
				);
	}

	@Inject
	HttpPluginDriverClient httpPluginDriverClient;

	@Inject
	TransactionInvoker transactionInvoker;

	@Inject
	ActorSystemProvider actorSystemProvider;

	@Inject
	RestHighLevelClient restHighLevelClient;
}
