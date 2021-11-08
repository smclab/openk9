package io.openk9.entity.manager.processor;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.impl.MapService;
import io.openk9.entity.manager.dto.Payload;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.HashMap;
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
	@Outgoing("index-writer")
	@Blocking
	public byte[] consume(byte[] bytes) {

		_entityManagerQueue.offer(
			new JsonObject(new String(bytes)).mapTo(Payload.class));

		return bytes;

	}

	HazelcastInstance _hazelcastInstance;

	private final IQueue<Payload> _entityManagerQueue;

}
