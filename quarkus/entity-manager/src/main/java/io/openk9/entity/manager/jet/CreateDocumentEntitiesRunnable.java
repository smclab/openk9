package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import com.hazelcast.projection.Projections;
import com.hazelcast.query.Predicates;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.model.graph.EntityGraph;
import io.openk9.entity.manager.service.graph.EntityGraphService;
import io.openk9.entity.manager.util.MapUtil;
import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.Collection;

public class CreateDocumentEntitiesRunnable
	implements StopWatchRunnable, HazelcastInstanceAware, Serializable {

	@Override
	public void run_() {

		_log.info("start CreateEntitiesRunnable");

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

		MultiMap<String, String> documentEntityMapMap =
			MapUtil.getDocumentEntityMapMap(_hazelcastInstance);

		EntityGraphService entityGraphService = CDI.current().select(
			EntityGraphService.class).get();

		for (String contentId : documentEntityMapMap.localKeySet()) {

			Collection<String> entityCacheIds =
				documentEntityMapMap.get(contentId);

			String[] arr = entityCacheIds.toArray(String[]::new);

			Collection<Object> project =
				entityIMap.project(
					Projections.singleAttribute("indexable"),
					Predicates.and(
						Predicates.in("cacheId", arr),
						Predicates.notEqual("id", null),
						Predicates.notEqual("graphId", null)
					)
				);

			if (project.size() == entityCacheIds.size()) {

				Collection<Entity> entities =
					entityIMap.values(Predicates.in("cacheId", arr));

				Long tenantId =
					entities
						.stream()
						.map(Entity::getTenantId)
						.findFirst()
						.orElse(-1L);

				EntityGraph entityGraph =
					entityGraphService.insertEntity(
						"document", EntityGraph.of(
							contentId, null, tenantId, contentId, "document")
					);

				for (Entity entity : entities) {
					entityGraphService.createRelationship(
						entityGraph.getId(), entity.getId(), "related_to");
				}

				documentEntityMapMap.remove(contentId);

			}
			
		}

	}

	@Override
	public void setHazelcastInstance(
		HazelcastInstance hazelcastInstance) {
		_hazelcastInstance = hazelcastInstance;
	}

	private transient HazelcastInstance _hazelcastInstance;

	private static final Logger _log = Logger.getLogger(
		CreateDocumentEntitiesRunnable.class);

}
