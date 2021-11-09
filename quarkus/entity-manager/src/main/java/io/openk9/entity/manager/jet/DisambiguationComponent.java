package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import io.openk9.entity.manager.service.DataService;
import io.openk9.entity.manager.service.EntityService;
import io.quarkus.runtime.Startup;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Startup
public class DisambiguationComponent {

	@PostConstruct
	public void init() {

		IAtomicLong schedulerAtomicLock =
			_hazelcastInstance
				.getCPSubsystem()
				.getAtomicLong("schedulerAtomicLock");

		long i = schedulerAtomicLock.getAndIncrement();

		if (i == 0) {
			IScheduledExecutorService createEntities =
				_hazelcastInstance.getScheduledExecutorService(
					"createEntities");

			createEntities.scheduleOnAllMembersAtFixedRate(
				new CreateEntitiesRunnable(), 0, 60, TimeUnit.SECONDS
			);

			IScheduledExecutorService associateEntities =
				_hazelcastInstance.getScheduledExecutorService(
					"associateEntities");

			associateEntities.scheduleOnAllMembersAtFixedRate(
				new AssociateEntitiesRunnable(), 60, 60, TimeUnit.SECONDS
			);
		}


	}

	@PreDestroy
	public void destroy() {

		IScheduledExecutorService createEntities =
			_hazelcastInstance.getScheduledExecutorService(
				"createEntities");

		createEntities.shutdown();

		IScheduledExecutorService associateEntities =
			_hazelcastInstance.getScheduledExecutorService(
				"associateEntities");

		associateEntities.shutdown();
	}

	@Inject
	HazelcastInstance _hazelcastInstance;

	@Inject
	EntityService _entityService;

	@Inject
	DataService _dataService;

	@Inject
	Logger _logger;

}
