package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.Pipelining;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicates;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.model.graph.EntityGraph;
import io.openk9.entity.manager.service.graph.EntityGraphService;
import io.openk9.entity.manager.util.MapUtil;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public class CreateEntitiesInGraphRunnable
	implements StopWatchRunnable, HazelcastInstanceAware, Serializable {

	@Override
	public void run_() {

		_log.info("start CreateEntitiesInGraphRunnable");

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

		Set<EntityKey> entityKeys = entityIMap.localKeySet(
			Predicates.and(
				Predicates.notEqual("id", null),
				Predicates.equal("graphId", null)
			)
		);

		Map<EntityKey, Entity> localEntityMap = entityIMap.getAll(entityKeys);

		_log.info("entityKeys: " + entityKeys.size());

		Pipelining<EntityGraph> entityGraphPipelining =
			new Pipelining<>(10);

		EntityGraphService entityGraphService = CDI.current().select(
			EntityGraphService.class).get();

		for (Map.Entry<EntityKey, Entity> entry : localEntityMap.entrySet()) {

			Entity v = entry.getValue();

			CompletionStage<EntityGraph> completionStage =
				entityGraphService.insertEntity(
					v.getType(),
					EntityGraph.of(
						v.getId(),
						null,
						v.getTenantId(),
						v.getName(),
						v.getType()
					)
				);

			try {
				entityGraphPipelining.add(completionStage);
			}
			catch (InterruptedException e) {
				_log.error(e.getMessage(), e);
			}

		}

		try {
			List<EntityGraph> results = entityGraphPipelining.results();

			Map<EntityKey, Entity> entityMapToUpdate = new HashMap<>();

			for (Map.Entry<EntityKey, Entity> entry : localEntityMap.entrySet()) {
				Entity value = entry.getValue();
				for (EntityGraph result : results) {
					if (value.getId().equals(result.getId())) {
						value.setGraphId(result.getGraphId());
						entityMapToUpdate.put(entry.getKey(), value);
						break;
					}
				}
			}

			entityIMap.setAll(entityMapToUpdate);

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

	private static final Logger _log = Logger.getLogger(
		CreateEntitiesInGraphRunnable.class);

}
