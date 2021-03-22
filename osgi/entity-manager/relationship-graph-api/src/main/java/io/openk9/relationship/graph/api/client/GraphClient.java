package io.openk9.relationship.graph.api.client;

import reactor.core.publisher.Flux;

import java.util.Map;

public interface GraphClient {

	Flux<Record> read(String query);

	Flux<Record> read(String query, Map<String, Object> params);

	Flux<Record> write(String query);

	Flux<Record> write(String query, Map<String, Object> params);

}
