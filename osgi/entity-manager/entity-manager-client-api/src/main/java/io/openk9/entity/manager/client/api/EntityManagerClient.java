package io.openk9.entity.manager.client.api;

import io.openk9.entity.manager.model.payload.Request;
import io.openk9.entity.manager.model.payload.Response;
import io.openk9.entity.manager.model.payload.ResponseList;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EntityManagerClient {
	Mono<ResponseList> getOrAddEntities(Request request);
}
