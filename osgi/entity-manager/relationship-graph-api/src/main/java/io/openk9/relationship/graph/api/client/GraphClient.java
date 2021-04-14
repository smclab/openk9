package io.openk9.relationship.graph.api.client;

import org.neo4j.cypherdsl.core.Statement;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public interface GraphClient {

	Flux<Record> write(Statement statement);

	Flux<Record> read(Statement statement);

}
