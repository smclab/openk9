package io.openk9.datasource.listener;

import akka.actor.typed.ActorSystem;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class SchedulerInitializerActor {

	@PostConstruct
	public void init() {
		this.actorSystem = ActorSystem.apply(
			Scheduler.create(httpPluginDriverClient), "datasource-scheduler");
	}

	public void scheduleDataSource(String tenantName, Datasource datasource) {
		actorSystem.tell(new Scheduler.ScheduleDatasource(tenantName, datasource));
	}

	public void unScheduleDataSource(String tenantName, Datasource datasource) {
		actorSystem.tell(new Scheduler.UnScheduleDatasource(tenantName, datasource));
	}

	public void triggerDataSource(String tenantName, Datasource datasource) {
		actorSystem.tell(new Scheduler.TriggerDatasource(tenantName, datasource));
	}

	private ActorSystem<Scheduler.Command> actorSystem;

	@Inject
	HttpPluginDriverClient httpPluginDriverClient;

}
