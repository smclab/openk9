package io.openk9.entity.manager.processor;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.impl.MapService;
import io.openk9.entity.manager.dto.Payload;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("/")
public class EntityManagerConsumer {

	public EntityManagerConsumer(HazelcastInstance hazelcastInstance) {
		_hazelcastInstance = hazelcastInstance;
		_entityManagerQueue = _hazelcastInstance.getQueue(
			"entityManagerQueue");
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
	public Message<JsonObject> consume(Object obj) throws InterruptedException {

		JsonObject jsonObject =
			obj instanceof JsonObject
				? (JsonObject)obj
				: new JsonObject(new String((byte[])obj));

		Payload payload = jsonObject.mapTo(Payload.class);

		_entityManagerQueue.offer(
			payload,
			45, TimeUnit.SECONDS);

		String replyTo = payload.getReplyTo();

		return Message.of(
			jsonObject, Metadata.of(
				new OutgoingRabbitMQMetadata.Builder()
					.withRoutingKey(replyTo)
					.withTimestamp(ZonedDateTime.now())
					.build()
			)
		);

	}

	HazelcastInstance _hazelcastInstance;

	private final IQueue<Payload> _entityManagerQueue;

}
