package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.Pipelining;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cache.model.EntityRelation;
import io.openk9.entity.manager.cache.model.EntityRelationKey;
import io.openk9.entity.manager.service.graph.EntityGraphService;
import io.openk9.entity.manager.util.MapUtil;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateRelationRunnable
	implements StopWatchRunnable, HazelcastInstanceAware, Serializable {

	@Override
	public void run_() {

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

		Set<EntityKey> entityKeys = entityIMap.localKeySet(
			Predicates.and(
				Predicates.notEqual("id", null),
				Predicates.notEqual("graphId", null)
			)
		);

		if (entityKeys.isEmpty()) {
			if (_log.isDebugEnabled()) {
				_log.debug("entityKeys is empty. skip");
			}
			return;
		}

		_log.info("entityKeys: " + entityKeys.size());

		Map<EntityKey, Entity> entityIMapAll = entityIMap.getAll(entityKeys);

		_log.info("entityIMapAll: " + entityIMapAll.size());

		IMap<EntityRelationKey, EntityRelation> entityRelationMap =
			MapUtil.getEntityRelationMap(_hazelcastInstance);

		Map<String, String> collect =
			entityIMapAll
				.values()
				.stream()
				.collect(Collectors.toMap(
					Entity::getCacheId, Entity::getId));

		String[] cacheIds = entityKeys
			.stream()
			.map(EntityKey::getCacheId)
			.distinct()
			.toArray(String[]::new);

		Map<EntityRelationKey, EntityRelation> entries =
			entityRelationMap.getAll(
				entityRelationMap.keySet(
					Predicates.in("__key.entityId", cacheIds))
			);

		_log.info("entityRelations: " + entries.size());

		EntityGraphService entityGraphService = CDI.current().select(
			EntityGraphService.class).get();

		List<EntityRelationKey> entityRelationKeysToDelete = new ArrayList<>();

		for (Map.Entry<EntityRelationKey, EntityRelation> entry : entries.entrySet()) {

			EntityRelationKey key = entry.getKey();
			EntityRelation value = entry.getValue();

			String from = collect.get(value.getEntityCacheId());
			String to = collect.get(value.getTo());

			if (from != null && to != null) {
				try {
					entityGraphService.createRelationship(
						from, to, value.getName()
					);

					entityRelationKeysToDelete.add(key);

				}
				catch (Exception e) {
					_log.error(e.getMessage(), e);
				}
			}

		}

		try {

			Pipelining pipelining = new Pipelining<>(10);

			for (EntityRelationKey entityRelationKey : entityRelationKeysToDelete) {
				pipelining.add(
					entityRelationMap.removeAsync(entityRelationKey)
				);
			}

			pipelining.results();

		}
		catch (Exception e) {
			_log.error(e.getMessage(), e);
		}

	}

	@Override
	public void setHazelcastInstance(
		HazelcastInstance hazelcastInstance) {
		_hazelcastInstance = hazelcastInstance;
	}

	private transient HazelcastInstance _hazelcastInstance;

	private static final Logger _log =
		Logger.getLogger(CreateRelationRunnable.class);

}
