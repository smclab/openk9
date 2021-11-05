package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cache.model.IngestionKey;
import io.openk9.entity.manager.service.DataService;
import io.openk9.entity.manager.service.EntityService;
import io.openk9.entity.manager.util.MapUtil;
import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class DisambiguationComponent {

	@Scheduled(every="20s")
	public void createEntities() {

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

		Set<EntityKey> entityKeys = entityIMap.localKeySet(
			Predicates.equal("id", null));

		entityIMap.executeOnKeys(
			entityKeys, new IndexEntityEntryProcessor());

	}

	@Scheduled(every="30s")
	public void associateEntities() {

		IMap<IngestionKey, Entity> ingestionMap =
			MapUtil.getIngestionMap(_hazelcastInstance);

		Set<IngestionKey> entityKeys = ingestionMap.localKeySet(
			Predicates.notEqual("this", null));

		ingestionMap.executeOnKeys(
			entityKeys, new AssociateEntityEntryProcessor());

		ingestionMap.removeAll(Predicates.equal("this", null));

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
