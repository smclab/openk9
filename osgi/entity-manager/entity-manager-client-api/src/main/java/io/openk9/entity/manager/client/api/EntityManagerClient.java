package io.openk9.entity.manager.client.api;

import io.openk9.entity.manager.model.payload.Request;
import io.openk9.entity.manager.model.payload.Response;
import reactor.core.publisher.Flux;

public interface EntityManagerClient {
	Flux<Response> getOrAddEntities(Request request);
}
