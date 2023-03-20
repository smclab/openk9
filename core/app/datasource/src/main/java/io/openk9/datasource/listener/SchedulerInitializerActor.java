package io.openk9.datasource.listener;

import akka.actor.typed.ActorSystem;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.sql.TransactionInvoker;
import org.hibernate.proxy.HibernateProxy;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.function.BiConsumer;

@ApplicationScoped
public class SchedulerInitializerActor {

	@PostConstruct
	public void init() {
		this.actorSystem = ActorSystem.apply(
			Scheduler.create(httpPluginDriverClient), "datasource-scheduler");
	}

	public void scheduleDataSource(String tenantName, Datasource datasource) {
		addLazyDependencyAndInvoke(
			tenantName, datasource, (a, b) -> actorSystem.tell(
				new Scheduler.ScheduleDatasource(a, b)));
	}

	public void unScheduleDataSource(String tenantName, Datasource datasource) {
		addLazyDependencyAndInvoke(
			tenantName, datasource, (a, b) -> actorSystem.tell(
				new Scheduler.UnScheduleDatasource(a, b)));
	}

	public void triggerDataSource(String tenantName, Datasource datasource) {
		addLazyDependencyAndInvoke(
			tenantName, datasource, (a, b) -> actorSystem.tell(
				new Scheduler.TriggerDatasource(a, b)));
	}

	public void addLazyDependencyAndInvoke(
		String tenantName,
		Datasource datasource, BiConsumer<String, Datasource> invoker) {

		PluginDriver pluginDriver = datasource.getPluginDriver();

		if (pluginDriver instanceof HibernateProxy) {
			HibernateProxy hibernateProxy =(HibernateProxy)pluginDriver;
			boolean uninitialized =
				hibernateProxy.getHibernateLazyInitializer().isUninitialized();
			if (uninitialized) {
				transactionInvoker.withStatelessTransaction(
					s -> Mutiny2.fetch(s, datasource.getPluginDriver()))
					.subscribe()
					.with((ignore) -> invoker.accept(tenantName, datasource));
				return;
			}
		}

		invoker.accept(tenantName, datasource);

	}

	private ActorSystem<Scheduler.Command> actorSystem;

	@Inject
	HttpPluginDriverClient httpPluginDriverClient;

	@Inject
	TransactionInvoker transactionInvoker;

}
