package io.openk9.entity.manager.processor;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionalMap;
import com.hazelcast.transaction.TransactionalMultiMap;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cache.model.EntityRelation;
import io.openk9.entity.manager.cache.model.EntityRelationKey;
import io.openk9.entity.manager.dto.EntityManagerRequest;
import io.openk9.entity.manager.dto.EntityRequest;
import io.openk9.entity.manager.dto.Payload;
import io.openk9.entity.manager.dto.RelationRequest;
import io.openk9.entity.manager.util.LoggerAggregator;
import io.quarkus.runtime.Startup;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
@Startup
public class EntityManagerBus {

	@PostConstruct
	public void afterCreate() {
		_entityFlakeId = _hazelcastInstance.getFlakeIdGenerator(
			"entityFlakeId");
		_entityRelationFlakeId = _hazelcastInstance.getFlakeIdGenerator(
			"entityRelationFlakeId");
		_entityManagerQueue = _hazelcastInstance.getQueue(
			"entityManagerQueue");
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

			Payload request = _entityManagerQueue.take();

			TransactionContext transactionContext =
				_hazelcastInstance.newTransactionContext();

			transactionContext.beginTransaction();

			try {
				TransactionalMap<EntityKey, Entity> entityTransactionalMap =
					transactionContext.getMap("entityMap");

				TransactionalMap<EntityRelationKey, EntityRelation> transactionalEntityRelationMap =
					transactionContext.getMap("entityRelationMap");

				TransactionalMultiMap<String, String> entityContextMap =
					transactionContext.getMultiMap("entityContextMap");

				EntityManagerRequest payload = request.getPayload();

				_loggerAggregator.emitLog(
					"process ingestionId", payload.getIngestionId());

				long tenantId = payload.getTenantId();
				String ingestionId = payload.getIngestionId();
				List<EntityRequest> entities = request.getEntities();

				Map<EntityKey, Entity> localEntityMap =
					new HashMap<>(entities.size());

				for (EntityRequest entityRequest : entities) {

					String name = entityRequest.getName();
					String type = entityRequest.getType();

					String cacheId = Long.toString(_entityFlakeId.newId());

					EntityKey key = EntityKey.of(tenantId, name, type, cacheId, ingestionId);

					Entity entity = new Entity(
						null, cacheId, tenantId, name, type, null,
						ingestionId, false);

					for (String context : entityRequest.getContext()) {
						entityContextMap.put(cacheId, context);
					}

					entityTransactionalMap.set(key, entity);

					localEntityMap.put(key, entity);

					for (EntityRequest entityRequest2 : entities) {

						for (RelationRequest relation : entityRequest2.getRelations()) {
							if (relation.getTo().equals(entityRequest.getTmpId())) {
								relation.setTo(entity.getCacheId());
							}
						}

					}

				}

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

						String to = relation.getTo();
						String name = relation.getName();

						for (Entity value : values) {
							if (value.getCacheId().equals(to)) {
								long entityRelationId = _entityRelationFlakeId.newId();

								EntityRelation entityRelation = new EntityRelation(
									entityRelationId, current.getCacheId(), ingestionId,
									name, value.getCacheId());

								transactionalEntityRelationMap.set(
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

			}
			catch (Exception e) {
				_log.error(e.getMessage(), e);
				transactionContext.rollbackTransaction();
			}
			finally {
				transactionContext.commitTransaction();
			}

		}
	}

	@Inject
	RestHighLevelClient _restHighLevelClient;

	@Inject
	Logger _log;

	@Inject
	HazelcastInstance _hazelcastInstance;

	@Inject
	LoggerAggregator _loggerAggregator;

	private ExecutorService _executorService;
	private FlakeIdGenerator _entityFlakeId;
	private FlakeIdGenerator _entityRelationFlakeId;
	private IQueue<Payload> _entityManagerQueue;

}
