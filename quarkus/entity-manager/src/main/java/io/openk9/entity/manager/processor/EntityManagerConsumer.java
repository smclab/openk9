package io.openk9.entity.manager.processor;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;
import com.hazelcast.map.impl.MapService;
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
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/")
public class EntityManagerConsumer {

	public EntityManagerConsumer(HazelcastInstance hazelcastInstance) {
		_hazelcastInstance = hazelcastInstance;
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

	@GET
	@Path("/map/{mapName}")
	public Object printIngestionMap(@PathParam("mapName") String mapName) {
		return new HashMap<>(_hazelcastInstance.getMap(mapName));
	}

	@GET
	@Path("/map")
	public Object printMapNames() {
		return _hazelcastInstance
			.getDistributedObjects()
			.stream()
			.filter(distributedObject -> distributedObject.getServiceName().equals(MapService.SERVICE_NAME))
			.map(DistributedObject::getName)
			.collect(Collectors.toList());
	}

	@Incoming("entity-manager-request")
	@Outgoing("index-writer")
	@Blocking
	public byte[] consume(byte[] bytes) {

		Payload request =
			new JsonObject(new String(bytes)).mapTo(Payload.class);

		EntityManagerRequest payload = request.getPayload();

		long tenantId = payload.getTenantId();
		String ingestionId = payload.getIngestionId();
		List<EntityRequest> entities = request.getEntities();

		Map<EntityKey, Entity> localEntityMap =
			new HashMap<>(entities.size());

		Map<EntityKey, Entity> localNewEntityMap =
			new HashMap<>();

		Map<IngestionKey, Entity> ingestionMap = new HashMap<>();

		for (EntityRequest entityRequest : entities) {

			String name = entityRequest.getName();
			String type = entityRequest.getType();
			long cacheId = _entityFlakeId.newId();
			Entity entity = new Entity(null, cacheId, tenantId, name, type);

			EntityKey key = EntityKey.of(tenantId, name, type);

			/*boolean lock = _entityMap.tryLock(key);

			if (lock) {

				try {

					if (_entityMap.containsKey(key)) {
						entity = _entityMap.get(key);
					}
					else {
						_entityMap.set(key, entity);
					}

				}
				finally {
					_entityMap.forceUnlock(key);
				}
			}
			else {
				_restEntityMultiMap.put(key, entity);
			}*/

			Entity entityInCache = _entityMap.get(key);

			if (entityInCache != null) {
				entity = entityInCache;
			}
			else {
				localNewEntityMap.put(key, entity);
			}

			localEntityMap.put(key, entity);

			IngestionKey ingestionKey = IngestionKey.of(
				entity.getCacheId(),
				ingestionId,
				tenantId);

			for (String c : entityRequest.getContext()) {
				_entityContextMultiMap.put(ingestionKey, c);
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

		_entityMap.setAll(localNewEntityMap);
		_ingestionMap.setAll(ingestionMap);
		_entityRelationMap.setAll(localEntityRelationMap);

		return bytes;

	}

	private final HazelcastInstance _hazelcastInstance;
	private final FlakeIdGenerator _entityFlakeId;
	private final FlakeIdGenerator _entityRelationFlakeId;
	private final IMap<EntityKey, Entity> _entityMap;
	private final MultiMap<EntityKey, Entity> _restEntityMultiMap;
	private final MultiMap<IngestionKey, String> _entityContextMultiMap;
	private final IMap<IngestionKey, Entity> _ingestionMap;
	private final IMap<EntityRelationKey, EntityRelation> _entityRelationMap;

}
