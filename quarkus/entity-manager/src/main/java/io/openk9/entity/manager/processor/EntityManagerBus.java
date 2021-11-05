package io.openk9.entity.manager.processor;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cache.model.EntityRelation;
import io.openk9.entity.manager.cache.model.EntityRelationKey;
import io.openk9.entity.manager.cache.model.IngestionKey;
import io.openk9.entity.manager.dto.EntityManagerRequest;
import io.openk9.entity.manager.dto.EntityRequest;
import io.openk9.entity.manager.dto.Payload;
import io.openk9.entity.manager.dto.RelationRequest;
import io.openk9.entity.manager.util.MapUtil;
import org.elasticsearch.client.RestHighLevelClient;
import org.jboss.logging.Logger;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

@ApplicationScoped
public class EntityManagerBus {

	public EntityManagerBus(HazelcastInstance hazelcastInstance) {
		_entityFlakeId = hazelcastInstance.getFlakeIdGenerator(
			"entityFlakeId");
		_entityRelationFlakeId = hazelcastInstance.getFlakeIdGenerator(
			"entityRelationFlakeId");
		_entityMap = MapUtil.getEntityMap(hazelcastInstance);
		_restEntityMultiMap = MapUtil.getRestEntityMultiMap(hazelcastInstance);
		_entityRelationMap = MapUtil.getEntityRelationMap(hazelcastInstance);
		_entityContextMultiMap = MapUtil.getEntityContextMultiMap(hazelcastInstance);
		_ingestionMap = MapUtil.getIngestionMap(hazelcastInstance);
	}

	public void emit(Payload request) {
		_many.tryEmitNext(request);
	}

	@PostConstruct
	public void init() {
		_many =
			Sinks
				.unsafe()
				.many()
				.unicast()
				.onBackpressureBuffer();

		_disposable = _many
			.asFlux()
			.flatMap(request -> Mono.defer(() -> {

			EntityManagerRequest payload = request.getPayload();

			long tenantId = payload.getTenantId();
			String ingestionId = payload.getIngestionId();
			List<EntityRequest> entities = request.getEntities();

			Map<EntityKey, Entity> localEntityMap =
				new HashMap<>(entities.size());

			Map<IngestionKey, Collection<? extends String>> entityContextMap =
				new HashMap<>();

			Map<IngestionKey, Entity> ingestionMap = new HashMap<>();

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

					for (String c : entityRequest.getContext()) {

						Collection<String> entityContextList =
							(Collection<String>)entityContextMap.computeIfAbsent(
								ingestionKey, k -> new ArrayList<>());

						entityContextList.add(c);

					}

					ingestionMap.put(ingestionKey, entity);

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

			CompletionStage<Void> future1 =
				_entityContextMultiMap.putAllAsync(entityContextMap);
			CompletionStage<Void> future3 =
				_ingestionMap.setAllAsync(ingestionMap);
			CompletionStage<Void> future4 =
				_entityRelationMap.setAllAsync(localEntityRelationMap);

			CompletableFuture<?>[] completableFutures = Stream
				.of(future1, future3, future4)
				.map(CompletionStage::toCompletableFuture)
				.toArray(CompletableFuture<?>[]::new);

			return Mono.fromCompletionStage(
				CompletableFuture.allOf(completableFutures));
			}))
			.subscribeOn(Schedulers.boundedElastic())
			.subscribe();

	}

	@PreDestroy
	public void destroy() {
		_disposable.dispose();
		_many.tryEmitComplete();
	}

	private void _manageExceptions(Throwable throwable, Object object) {

		if (_log.isEnabled(Logger.Level.ERROR)) {
			if (object == null) {
				_log.error(throwable.getMessage(), throwable);
			}
			else {
				_log.error(
					"error on object: { " + object.toString() + " }",
					throwable);
			}
		}

	}

	private Sinks.Many<Payload> _many;
	private Disposable _disposable;

	@Inject
	RestHighLevelClient _restHighLevelClient;

	@Inject
	Logger _log;

	private final FlakeIdGenerator _entityFlakeId;
	private final FlakeIdGenerator _entityRelationFlakeId;
	private final IMap<EntityKey, Entity> _entityMap;
	private final MultiMap<EntityKey, Entity> _restEntityMultiMap;
	private final MultiMap<IngestionKey, String> _entityContextMultiMap;
	private final IMap<IngestionKey, Entity> _ingestionMap;
	private final IMap<EntityRelationKey, EntityRelation> _entityRelationMap;

}
