package io.openk9.relationship.graph.api.client;

import org.neo4j.cypherdsl.core.Statement;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GraphClient {

	<T> Mono<T> makeTransactional(Mono<T> mono);

	<T> Flux<T> makeTransactional(Flux<T> flux);

	Flux<Record> write(Statement statement);

	Flux<Record> read(Statement statement);

}
