package io.openk9.neo4j.relationship.graph.client;

import io.openk9.neo4j.relationship.graph.Neo4jGraphConnection;
import io.openk9.relationship.graph.api.client.GraphClient;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.reactive.RxSession;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.function.Function;

@Component(
	immediate = true,
	service = GraphClient.class
)
public class Neo4jGraphClient implements GraphClient {

	@Override
	public Flux<io.openk9.relationship.graph.api.client.Record> read(
		String query) {

		Driver driver = _neo4jGraphConnection.getDriver();

		return exec(
			driver,
			rxSession ->
				rxSession.readTransaction(
					tx -> tx.run(query).records()))
			.map(RecordWrapper::new);
	}

	@Override
	public Flux<io.openk9.relationship.graph.api.client.Record> read(
		String query, Map<String, Object> params) {

		Driver driver = _neo4jGraphConnection.getDriver();

		return exec(
			driver,
			rxSession ->
				rxSession.readTransaction(
					tx -> tx.run(query, params).records()))
			.map(RecordWrapper::new);

	}

	@Override
	public Flux<io.openk9.relationship.graph.api.client.Record> write(
		String query) {

		Driver driver = _neo4jGraphConnection.getDriver();

		return exec(
			driver,
			rxSession ->
				rxSession.writeTransaction(
					tx -> tx.run(query).records()))
			.map(RecordWrapper::new);

	}

	@Override
	public Flux<io.openk9.relationship.graph.api.client.Record> write(
		String query, Map<String, Object> params) {

		Driver driver = _neo4jGraphConnection.getDriver();

		return exec(
			driver,
			rxSession ->
				rxSession.writeTransaction(
					tx -> tx.run(query, params).records()))
			.map(RecordWrapper::new);
	}

	private Flux<Record> exec(
		Driver driver, Function<RxSession, Publisher<Record>> sourceSupplier) {

		return Flux.using(
			driver::rxSession,
			sourceSupplier,
			RxSession::close
		);

	}

	@Reference
	private Neo4jGraphConnection _neo4jGraphConnection;

}
