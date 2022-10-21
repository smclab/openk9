/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.entity.manager.processor;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.impl.MapService;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionalMap;
import com.hazelcast.transaction.TransactionalMultiMap;
import io.openk9.entity.manager.cache.model.DocumentKey;
import io.openk9.entity.manager.cache.model.Entity;
import io.openk9.entity.manager.cache.model.EntityKey;
import io.openk9.entity.manager.cache.model.EntityRelation;
import io.openk9.entity.manager.cache.model.EntityRelationKey;
import io.openk9.entity.manager.dto.EntityManagerRequest;
import io.openk9.entity.manager.dto.EntityRequest;
import io.openk9.entity.manager.dto.Payload;
import io.openk9.entity.manager.dto.RelationRequest;
import io.openk9.entity.manager.util.LoggerAggregator;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.elasticsearch.client.RestHighLevelClient;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/")
public class EntityManagerConsumer {

	@PostConstruct
	public void afterCreate() {
		_entityFlakeId = _hazelcastInstance.getFlakeIdGenerator(
			"entityFlakeId");
		_entityRelationFlakeId = _hazelcastInstance.getFlakeIdGenerator(
			"entityRelationFlakeId");
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
	@Outgoing("entity-manager-response")
	@Blocking
	public Message<JsonObject> consume(Message<Object> message) {

		Object obj = message.getPayload();

		JsonObject jsonObject =
			obj instanceof JsonObject
				? (JsonObject)obj
				: new JsonObject(new String((byte[])obj));

		Payload request = jsonObject.mapTo(Payload.class);

		TransactionContext transactionContext =
			_hazelcastInstance.newTransactionContext();

		transactionContext.beginTransaction();

		try {
			TransactionalMap<EntityKey, Entity> entityTransactionalMap =
				transactionContext.getMap("entityMap");

			TransactionalMap<EntityRelationKey, EntityRelation> transactionalEntityRelationMap =
				transactionContext.getMap("entityRelationMap");

			TransactionalMultiMap<DocumentKey, String> documentEntityMap =
				transactionContext.getMultiMap("documentEntityMap");

			EntityManagerRequest payload = request.getPayload();

			_loggerAggregator.emitLog(
				"process ingestionId", payload.getIngestionId());

			long tenantId = payload.getTenantId();
			String ingestionId = payload.getIngestionId();
			List<EntityRequest> entities = payload.getEntities();

			Map<EntityKey, Entity> localEntityMap =
				new HashMap<>(entities.size());

			for (EntityRequest entityRequest : entities) {

				String name = entityRequest.getName();
				String type = entityRequest.getType();

				String cacheId = Long.toString(_entityFlakeId.newId());

				EntityKey key = EntityKey.of(tenantId, name, type, cacheId, ingestionId);

				Entity entity = new Entity(
					null, cacheId, tenantId, name, type, null,
					ingestionId, false, true, entityRequest.getContext());

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
									current.getCacheId(),
									ingestionId
								),
								entityRelation
							);
						}
					}
				}
			}

			if (!localEntityMap.isEmpty()) {

				DocumentKey key = DocumentKey.of(
					payload.getDatasourceId(),
					payload.getContentId(),
					tenantId);

				for (Entity value : localEntityMap.values()) {
					documentEntityMap.put(key, value.getCacheId());
				}
			}

		}
		catch (Exception e) {
			transactionContext.rollbackTransaction();
			if (e instanceof RuntimeException) {
				throw (RuntimeException)e;
			}
			throw new RuntimeException(e);
		}
		finally {
			transactionContext.commitTransaction();
		}

		String replyTo = request.getReplyTo();

		message.ack();

		return Message.of(
			jsonObject, Metadata.of(
				new OutgoingRabbitMQMetadata.Builder()
					.withRoutingKey(replyTo)
					.withTimestamp(ZonedDateTime.now())
					.build()
			)
		);

	}

	@Inject
	HazelcastInstance _hazelcastInstance;

	@Inject
	RestHighLevelClient _restHighLevelClient;

	@Inject
	Logger _log;

	@Inject
	LoggerAggregator _loggerAggregator;

	private FlakeIdGenerator _entityFlakeId;
	private FlakeIdGenerator _entityRelationFlakeId;

}
