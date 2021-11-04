package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cache.model.IngestionKey;
import io.openk9.entity.manager.model.EntityIndex;
import io.openk9.entity.manager.service.DataService;
import io.openk9.entity.manager.service.EntityService;
import io.openk9.entity.manager.util.MapUtil;
import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class DisambiguationComponent {

	@Scheduled(every="20s")
	public void createEntities() {

		_logger.info("in createEntities");

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

		Set<EntityKey> entityKeys = entityIMap.localKeySet();

		Predicate predicateNameType = entityKeys
			.stream()
			.map(entityKey -> Predicates.and(
				Predicates.equal("__key.name", entityKey.getName()),
				Predicates.equal("__key.type", entityKey.getType())))
			.reduce(Predicates::and)
			.map(p -> Predicates.and(Predicates.equal("id", null), p))
			.orElseGet(Predicates::alwaysFalse);

		Set<Map.Entry<EntityKey, Entity>> entrySet =
			entityIMap.entrySet(predicateNameType);

		_logger.info("[createEntities] Entities size: " + entrySet.size());

		for (Map.Entry<EntityKey, Entity> entry : entrySet) {

			Entity v = entry.getValue();

			EntityService entityService = _entityService;

			try {
				entityService.index(
					EntityIndex.of(
						v.getCacheId(),
						v.getTenantId(),
						v.getName(),
						v.getType())
				);
			}
			catch (Exception ioe) {
				_logger.error(ioe.getMessage(), ioe);
			}

			v.setId(v.getCacheId());

			entityIMap.set(entry.getKey(), v);

			_logger.info("[createEntities] Update entity: " + entry.getKey());

		}

		entityIMap.executeOnEntries(
			new IndexEntityEntryProcessor(),
			predicateNameType);

	}

	@Scheduled(every="30s")
	public void associateEntities() {

		_logger.info("in associateEntities");

		IMap<IngestionKey, Entity> ingestionMap =
			MapUtil.getIngestionMap(_hazelcastInstance);

		Set<IngestionKey> entityKeys = ingestionMap.localKeySet();

		Predicate predicateNameType =
			entityKeys
				.stream()
				.map(ingestionKey -> Predicates.and(
					Predicates.equal("__key.ingestionId", ingestionKey.getIngestionId()),
					Predicates.equal("__key.tenantId", ingestionKey.getTenantId()),
					Predicates.equal("__key.entityId", ingestionKey.getEntityId())
				))
				.reduce(Predicates::and)
				.orElseGet(Predicates::alwaysFalse);

		Set<Map.Entry<IngestionKey, Entity>> entrySet =
			ingestionMap.entrySet(predicateNameType);

		for (Map.Entry<IngestionKey, Entity> entry : entrySet) {

			IngestionKey k = entry.getKey();
			Entity v = entry.getValue();

			MultiMap<IngestionKey, String> entityContextMultiMap =
				MapUtil.getEntityContextMultiMap(_hazelcastInstance);

			DataService dataService = _dataService;

			try {

				dataService.associateEntity(
					v.getTenantId(),
					k.getIngestionId(),
					v,
					entityContextMultiMap.get(k)
				);

				ingestionMap.delete(k);

			}
			catch (Exception ioe) {
				_logger.error(ioe.getMessage(), ioe);
			}

		}

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
