package io.openk9.neo4j.relationship.graph.client;

import io.openk9.neo4j.relationship.graph.Neo4jGraphConnection;
import io.openk9.relationship.graph.api.client.GraphClient;
import io.openk9.relationship.graph.api.client.Record;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.driver.Driver;
import org.neo4j.driver.reactive.RxSession;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Component(
	immediate = true,
	service = GraphClient.class
)
public class Neo4jGraphClient implements GraphClient {

	@Override
	public Publisher<Record> write(Statement statement) {
		return _exec(
			_neo4jGraphConnection.getDriver(),
			rxSession -> rxSession.writeTransaction(
				tx -> tx.run(statement.getCypher()).records())
		)
			.map(RecordWrapper::new);
	}

	@Override
	public Publisher<Record> read(Statement statement) {
		return _exec(
			_neo4jGraphConnection.getDriver(),
			rxSession -> rxSession.readTransaction(
				tx -> tx.run(statement.getCypher()).records())
		)
			.map(RecordWrapper::new);
	}

	private Flux<org.neo4j.driver.Record> _exec(
		Driver driver, Function<RxSession,
		Publisher<org.neo4j.driver.Record>> sourceSupplier) {

		return Flux.using(
			driver::rxSession,
			sourceSupplier,
			RxSession::close
		);

	}

	@Reference
	private Neo4jGraphConnection _neo4jGraphConnection;

}
