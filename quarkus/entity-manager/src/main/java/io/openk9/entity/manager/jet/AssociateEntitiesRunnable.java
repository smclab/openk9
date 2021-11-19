package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import com.hazelcast.query.Predicates;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cache.model.IngestionEntity;
import io.openk9.entity.manager.service.index.DataService;
import io.openk9.entity.manager.util.MapUtil;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AssociateEntitiesRunnable
	implements StopWatchRunnable, HazelcastInstanceAware, Serializable {

	@Override
	public void run_() {

		_log.info("start AssociateEntitiesRunnable");

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

		MultiMap<String, String> entityContextMap =
			_hazelcastInstance.getMultiMap("entityContextMap");

		Set<EntityKey> entityKeys = entityIMap.localKeySet(
			Predicates.and(
				Predicates.equal("indexable", true),
				Predicates.notEqual("id", null),
				Predicates.notEqual("graphId", null),
				Predicates.equal("associated", false)
			)
		);

		Map<EntityKey, Entity> localEntityMap =
			entityIMap.getAll(entityKeys);

		_log.info("ingestionKeys: " + localEntityMap.size());

		Map<String, List<Entity>> groupingByIngestionId =
			localEntityMap
				.entrySet()
				.stream()
				.collect(
					Collectors.groupingBy(
						e -> e.getKey().getIngestionId(),
						Collectors.mapping(
							Map.Entry::getValue, Collectors.toList())));


		Map<EntityKey, Entity> entitiesToUpdate = new HashMap<>();

		for (Map.Entry<String, List<Entity>> entry : groupingByIngestionId.entrySet()) {

			String ingestionId = entry.getKey();
			List<Entity> v = entry.getValue();

			if (v.isEmpty()) {
				continue;
			}

			DataService dataService =
				CDI.current().select(DataService.class).get();

			Long tenantId =
				v
					.stream()
					.map(Entity::getTenantId)
					.findFirst()
					.get();

			try {

				boolean associated =
					dataService.associateEntities(
						tenantId,
						ingestionId,
						v
							.stream()
							.map(entity -> IngestionEntity
								.fromEntity(entity, entityContextMap.get(entity.getCacheId())))
							.collect(Collectors.toList())
					);

				if (associated) {
					for (Entity entity : v) {
						entity.setAssociated(true);
						entitiesToUpdate.put(
							EntityKey.of(
								entity.getTenantId(), entity.getName(),
								entity.getType(), entity.getCacheId(),
								entity.getIngestionId()
							), entity);
					}
				}
			}
			catch (Exception ioe) {
				_log.error(ioe.getMessage());
			}

		}

		entityIMap.setAll(entitiesToUpdate);

	}

	@Override
	public void setHazelcastInstance(
		HazelcastInstance hazelcastInstance) {
		_hazelcastInstance = hazelcastInstance;
	}

	private transient HazelcastInstance _hazelcastInstance;

	private static final Logger _log = Logger.getLogger(
		AssociateEntitiesRunnable.class);

}
