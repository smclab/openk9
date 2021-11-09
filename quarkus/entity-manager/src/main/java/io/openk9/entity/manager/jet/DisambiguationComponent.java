package io.openk9.entity.manager.jet;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;
import com.hazelcast.scheduledexecutor.IScheduledFuture;
import io.openk9.entity.manager.service.index.DataService;
import io.openk9.entity.manager.service.index.EntityService;
import io.quarkus.runtime.Startup;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Startup
public class DisambiguationComponent {

	@PostConstruct
	public void init() {

		Member localMember = _hazelcastInstance.getCluster().getLocalMember();

		IScheduledExecutorService createEntities =
			_hazelcastInstance.getScheduledExecutorService(
				"createEntities");

		_disposableList.add(
			createEntities.scheduleOnMemberAtFixedRate(
				new CreateEntitiesRunnable(), localMember, 0, 60,
				TimeUnit.SECONDS
			)
		);

		IScheduledExecutorService associateEntities =
			_hazelcastInstance.getScheduledExecutorService(
				"associateEntities");

		_disposableList.add(
			associateEntities.scheduleOnMemberAtFixedRate(
				new AssociateEntitiesRunnable(), localMember, 60, 60,
				TimeUnit.SECONDS
			)
		);

		IScheduledExecutorService createEntitiesInGraph =
			_hazelcastInstance.getScheduledExecutorService(
				"createEntitiesInGraph");

		_disposableList.add(
			createEntitiesInGraph.scheduleOnMemberAtFixedRate(
				new CreateEntitiesInGraphRunnable(), localMember, 0, 60,
				TimeUnit.SECONDS
			)
		);

		IScheduledExecutorService createRelation =
			_hazelcastInstance.getScheduledExecutorService(
				"createRelation");

		_disposableList.add(
			createRelation.scheduleOnMemberAtFixedRate(
				new CreateRelationRunnable(), localMember, 0, 60,
				TimeUnit.SECONDS
			)
		);

	}


	@PreDestroy
	public void destroy() {
		_disposableList.forEach(IScheduledFuture::dispose);
		_disposableList.clear();
	}

	@Inject
	HazelcastInstance _hazelcastInstance;

	@Inject
	EntityService _entityService;

	@Inject
	DataService _dataService;

	@Inject
	Logger _logger;

	private final List<IScheduledFuture<?>> _disposableList = new ArrayList<>();

}
