package io.openk9.datasource.listener;

import akka.actor.typed.ActorSystem;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.sql.TransactionInvoker;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SchedulerInitializerActor {

	@PostConstruct
	public void init() {
		this.actorSystem = ActorSystem.apply(
			Scheduler.create(httpPluginDriverClient, transactionInvoker), "datasource-scheduler");
	}

	public void scheduleDataSource(String tenantName, long datasourceId) {
		actorSystem.tell(new Scheduler.ScheduleDatasource(tenantName, datasourceId));
	}

	public void unScheduleDataSource(String tenantName, long datasourceId) {
		actorSystem.tell(new Scheduler.UnScheduleDatasource(tenantName, datasourceId));
	}

	public void triggerDataSource(String tenantName, long datasourceId) {
		actorSystem.tell(new Scheduler.TriggerDatasource(tenantName, datasourceId));
	}

	private ActorSystem<Scheduler.Command> actorSystem;

	@Inject
	HttpPluginDriverClient httpPluginDriverClient;

	@Inject
	TransactionInvoker transactionInvoker;

}
