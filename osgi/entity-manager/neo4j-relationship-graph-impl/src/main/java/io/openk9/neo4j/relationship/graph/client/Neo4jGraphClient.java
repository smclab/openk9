package io.openk9.neo4j.relationship.graph.client;

import io.openk9.neo4j.relationship.graph.Neo4jGraphConnection;
import io.openk9.relationship.graph.api.client.GraphClient;
import io.openk9.relationship.graph.api.client.Record;
import org.neo4j.cypherdsl.core.Statement;
import org.neo4j.driver.Driver;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.reactive.RxSession;
import org.neo4j.driver.reactive.RxTransaction;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;
import java.util.function.Function;

@Component(
	immediate = true,
	service = GraphClient.class
)
public class Neo4jGraphClient implements GraphClient {

	@Override
	public <T> Mono<T> makeTransactional(Mono<T> mono) {
		return Mono
			.usingWhen(
				Mono.fromSupplier(this::createSession),
				rxSession -> Mono.usingWhen(
					rxSession.beginTransaction(),
					rxTransaction -> mono.contextWrite(
						Context.of(SESSION, rxTransaction)),
					RxTransaction::commit,
					(rxTransaction, e) -> {
						_log.error(e.getMessage(), e);
						return rxTransaction.rollback();
					},
					RxTransaction::rollback
				),
				RxSession::close
			);
	}

	@Override
	public <T> Flux<T> makeTransactional(Flux<T> flux) {
		return Flux
			.usingWhen(
				Mono.fromSupplier(this::createSession),
				rxSession -> Flux.usingWhen(
					rxSession.beginTransaction(),
					rxTransaction -> flux.contextWrite(
						Context.of(SESSION, rxTransaction)),
					RxTransaction::commit,
					(rxTransaction, e) -> {
						_log.error(e.getMessage(), e);
						return rxTransaction.rollback();
					},
					RxTransaction::rollback
				),
				RxSession::close
			);
	}

	protected RxSession createSession() {
		return _driverProvider.getDriver().rxSession();
	}

	protected RxSession createSession(SessionConfig sessionConfig) {
		return _driverProvider.getDriver().rxSession();
	}

	@Override
	public Flux<Record> write(Statement statement) {
		return Flux.deferContextual(contextView -> {

			Optional<RxTransaction> optional =
				contextView.getOrEmpty(SESSION);

			return optional
				.map(rxTransaction -> rxTransaction
					.run(statement.getCypher())
					.records()
				)
				.map(Flux::from)
				.orElseGet(() -> _exec(
					_driverProvider.getDriver(),
					rxSession -> rxSession.writeTransaction(
						tx -> tx.run(statement.getCypher()).records())
				))
				.map(RecordWrapper::new);

		});
	}

	@Override
	public Flux<Record> read(Statement statement) {
		return Flux.deferContextual(contextView -> {

			Optional<RxTransaction> optional =
				contextView.getOrEmpty(SESSION);

			return optional
				.map(rxTransaction -> rxTransaction
					.run(statement.getCypher())
					.records()
				)
				.map(Flux::from)
				.orElseGet(() -> _exec(
					_driverProvider.getDriver(),
					rxSession -> rxSession.readTransaction(
						tx -> tx.run(statement.getCypher()).records())
				))
				.map(RecordWrapper::new);

		});
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
	private Neo4jGraphConnection _driverProvider;

	protected static final String SESSION = "session";

	private static final Logger _log =
		LoggerFactory.getLogger(Neo4jGraphClient.class);

}
