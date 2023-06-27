package io.openk9.datasource.listener;

import akka.actor.typed.ActorRef;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import io.openk9.datasource.actor.ActorSystemProvider;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.sql.TransactionInvoker;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SchedulerInitializerActor {

	public void scheduleDataSource(String tenantName, long datasourceId, boolean schedulable, String cron) {
		getSchedulerRef().tell(new JobTriggerer.ScheduleDatasource(tenantName, datasourceId, schedulable, cron));
	}

	public void unScheduleDataSource(String tenantName, long datasourceId) {
		getSchedulerRef().tell(new JobTriggerer.UnScheduleDatasource(tenantName, datasourceId));
	}

	public void triggerDataSource(
		String tenantName, long datasourceId, boolean startFromFirst) {
		getSchedulerRef().tell(new JobTriggerer.TriggerDatasource(tenantName, datasourceId, startFromFirst));
	}

	private ActorRef<JobTriggerer.Command> getSchedulerRef() {
		return ClusterSingleton.get(actorSystemProvider.getActorSystem())
			.init(
				SingletonActor.of(
					JobTriggerer.create(
						httpPluginDriverClient, transactionInvoker
					), "scheduler")
				);
	}

	@Inject
	HttpPluginDriverClient httpPluginDriverClient;

	@Inject
	TransactionInvoker transactionInvoker;

	@Inject
	ActorSystemProvider actorSystemProvider;

}
