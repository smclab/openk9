package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import com.hazelcast.query.Predicate;
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
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@ApplicationScoped
public class DisambiguationComponent {

	@Scheduled(every="20s")
	public void createEntities() {

		IMap<EntityKey, Entity> entityIMap =
			MapUtil.getEntityMap(_hazelcastInstance);

		Set<EntityKey> entityKeys = entityIMap.localKeySet();

		Predicate predicateIdNull = Predicates.equal("id", null);

		Stream<Predicate> predicateStream1 =
			entityKeys
				.stream()
				.map(entityKey -> Predicates.and(
					Predicates.equal("__key.name", entityKey.getName()),
					Predicates.equal("__key.type", entityKey.getType())));

		Predicate[] predicates =
			Stream.concat(Stream.of(predicateIdNull), predicateStream1).toArray(
				Predicate[]::new);

		entityIMap.executeOnEntries(
			new IndexEntityEntryProcessor(), Predicates.and(predicates));

	}

	@Scheduled(every="30s")
	public void associateEntities() {

		IMap<IngestionKey, Entity> ingestionMap =
			MapUtil.getIngestionMap(_hazelcastInstance);

		Set<IngestionKey> entityKeys = ingestionMap.localKeySet();

		Map<IngestionKey, Entity> entityMap = ingestionMap.getAll(entityKeys);

		for (Map.Entry<IngestionKey, Entity> entry : entityMap.entrySet()) {

			IngestionKey k = entry.getKey();
			Entity v = entry.getValue();

			if (v != null) {

				MultiMap<IngestionKey, String> entityContextMultiMap =
					MapUtil.getEntityContextMultiMap(_hazelcastInstance);

				DataService dataService =
					CDI.current().select(DataService.class).get();

				try {

					boolean associated =
						dataService.associateEntity(
							v.getTenantId(),
							k.getIngestionId(),
							v,
							entityContextMultiMap.get(k)
						);

					if (associated) {
						ingestionMap.delete(k);
					}
				}
				catch (Exception ioe) {
					_logger.error(ioe.getMessage(), ioe);
				}

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
