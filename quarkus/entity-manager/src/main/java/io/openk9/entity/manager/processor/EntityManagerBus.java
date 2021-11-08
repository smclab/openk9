package io.openk9.entity.manager.processor;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cache.model.EntityRelation;
import io.openk9.entity.manager.cache.model.EntityRelationKey;
import io.openk9.entity.manager.cache.model.IngestionEntity;
import io.openk9.entity.manager.cache.model.IngestionKey;
import io.openk9.entity.manager.dto.EntityManagerRequest;
import io.openk9.entity.manager.dto.EntityRequest;
import io.openk9.entity.manager.dto.Payload;
import io.openk9.entity.manager.dto.RelationRequest;
import io.openk9.entity.manager.util.MapUtil;
import lombok.SneakyThrows;
import org.elasticsearch.client.RestHighLevelClient;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

@ApplicationScoped
public class EntityManagerBus {

	public EntityManagerBus(HazelcastInstance hazelcastInstance) {
		_entityFlakeId = hazelcastInstance.getFlakeIdGenerator(
			"entityFlakeId");
		_entityRelationFlakeId = hazelcastInstance.getFlakeIdGenerator(
			"entityRelationFlakeId");
		_entityMap = MapUtil.getEntityMap(hazelcastInstance);
		_entityRelationMap = MapUtil.getEntityRelationMap(hazelcastInstance);
		_ingestionMap = MapUtil.getIngestionMap(hazelcastInstance);
		_entityManagerQueue = hazelcastInstance.getQueue(
			"entityManagerQueue");
	}

	@PostConstruct
	public void afterCreate() {
		_executorService = Executors.newSingleThreadExecutor();
		_executorService.execute(this::run);
	}

	@PreDestroy
	public void beforeDestroy() {
		_executorService.shutdown();
	}

	@SneakyThrows
	public void run() {
		while (true) {

			_log.info("START take");

			Payload request = _entityManagerQueue.take();

			EntityManagerRequest payload = request.getPayload();

			_log.info("process ingestionId: " + payload.getIngestionId());

			long tenantId = payload.getTenantId();
			String ingestionId = payload.getIngestionId();
			List<EntityRequest> entities = request.getEntities();

			Map<EntityKey, Entity> localEntityMap =
				new HashMap<>(entities.size());

			Map<IngestionKey, IngestionEntity> ingestionMap = new HashMap<>();

			for (EntityRequest entityRequest : entities) {

				String name = entityRequest.getName();
				String type = entityRequest.getType();

				EntityKey key = EntityKey.of(tenantId, name, type);

				Entity entity;

				boolean lock = _entityMap.tryLock(key);

				if (lock) {

					try {
						entity = _entityMap.get(key);

						if (entity == null) {
							long cacheId = _entityFlakeId.newId();
							entity = new Entity(null, cacheId, tenantId, name, type);
							_entityMap.set(key, entity);
						}

						localEntityMap.put(key, entity);

					}
					finally {
						_entityMap.forceUnlock(key);
					}

					IngestionKey ingestionKey = IngestionKey.of(
						entity.getCacheId(),
						ingestionId,
						tenantId);

					ingestionMap.put(
						ingestionKey,
						IngestionEntity.fromEntity(
							entity, entityRequest.getContext())
					);

					for (EntityRequest entityRequest2 : entities) {

						for (RelationRequest relation : entityRequest2.getRelations()) {
							if (relation.getTo().equals(entityRequest.getTmpId())) {
								relation.setTo(entity.getCacheId());
							}
						}

					}

				}
				else {
					/*
					_restEntityMultiMap.put(
						 key, new Entity(null, null, tenantId, name, type));
					*/

				}

			}

			Map<EntityRelationKey, EntityRelation> localEntityRelationMap =
				new HashMap<>();

			for (EntityRequest entity : entities) {

				List<RelationRequest> relations = entity.getRelations();

				if (relations == null || relations.isEmpty()) {
					continue;
				}

				Collection<Entity> values = localEntityMap.values();

				Entity current =
					values
						.stream()
						.filter(e -> e.getName().equals(entity.getName()) &&
									 e.getType().equals(entity.getType()))
						.findFirst()
						.orElse(null);

				if (current == null) {
					continue;
				}

				for (RelationRequest relation : relations) {

					Long to = relation.getTo();
					String name = relation.getName();

					for (Entity value : values) {
						if (value.getCacheId().equals(to)) {
							long entityRelationId = _entityRelationFlakeId.newId();

							EntityRelation entityRelation = new EntityRelation(
								entityRelationId, current.getCacheId(), ingestionId,
								name, value.getCacheId());

							localEntityRelationMap.put(
								EntityRelationKey.of(
									entityRelationId,
									current.getCacheId()
								),
								entityRelation
							);
						}
					}
				}
			}

			CompletionStage<Void> future3 =
				_ingestionMap.setAllAsync(ingestionMap);
			CompletionStage<Void> future4 =
				_entityRelationMap.setAllAsync(localEntityRelationMap);

			CompletableFuture<?>[] completableFutures = Stream
				.of(future3, future4)
				.map(CompletionStage::toCompletableFuture)
				.toArray(CompletableFuture<?>[]::new);

			CompletableFuture.allOf(completableFutures).join();
		}
	}

	@Inject
	RestHighLevelClient _restHighLevelClient;

	@Inject
	Logger _log;

	private final FlakeIdGenerator _entityFlakeId;
	private final FlakeIdGenerator _entityRelationFlakeId;
	private final IMap<EntityKey, Entity> _entityMap;
	private final IMap<IngestionKey, IngestionEntity> _ingestionMap;
	private final IMap<EntityRelationKey, EntityRelation> _entityRelationMap;
	private final IQueue<Payload> _entityManagerQueue;

	private ExecutorService _executorService;

}
