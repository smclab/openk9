package io.openk9.entity.manager.jet;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
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
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class DisambiguationComponent {

	@Scheduled(every="20s")
	public void createEntities() {

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

		entityIMap.executeOnEntries(
			new IndexEntityEntryProcessor(),
			predicateNameType);

	}

	@Scheduled(every="30s")
	public void associateEntities() {

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

		ingestionMap.executeOnEntries(
			new AssociateEntityEntryProcessor(),
			predicateNameType);

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
