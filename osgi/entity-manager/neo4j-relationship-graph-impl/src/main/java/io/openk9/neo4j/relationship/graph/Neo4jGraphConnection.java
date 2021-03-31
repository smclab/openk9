package io.openk9.neo4j.relationship.graph;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component(
	immediate = true,
	service = Neo4jGraphConnection.class
)
public class Neo4jGraphConnection {

	@interface Config {
		String uri() default "bolt://neo4j";
		String username() default "openk9";
		String password() default "openk9";
		boolean logLeakedSessions() default false;
		int maxConnectionPoolSize() default 100; //PoolSettings.DEFAULT_MAX_CONNECTION_POOL_SIZE;
		long idleTimeBeforeConnectionTest() default -1; //PoolSettings.DEFAULT_IDLE_TIME_BEFORE_CONNECTION_TEST;
		long maxConnectionLifetimeMillis() default 3_600_000; // TimeUnit.HOURS.toMillis( 1 )
		long connectionAcquisitionTimeoutMillis() default 60_000; // TimeUnit.SECONDS.toMillis( 60 )
		int routingFailureLimit() default 1; // RoutingSettings.DEFAULT.maxRoutingFailures()
		long routingRetryDelayMillis() default 5_000; // RoutingSettings.DEFAULT.retryTimeoutDelay();
		long routingTablePurgeDelayMillis() default 30_000; // RoutingSettings.DEFAULT.routingTablePurgeDelayMs()
		int connectionTimeoutMillis() default 30_000; // TimeUnit.SECONDS.toMillis( 30 )
		long maxTransactionRetryTime() default 30_000; // RetrySettings.DEFAULT
		boolean isMetricsEnabled() default false;
		long fetchSize() default 1000; // FetchSizeUtil.DEFAULT_FETCH_SIZE;
		int eventLoopThreads() default -1; // 2 * NumberOfProcessors
	}

	@Activate
	void activate(Config config) {

		org.neo4j.driver.Config.ConfigBuilder builder =
			org.neo4j.driver.Config
				.builder()
				.withConnectionAcquisitionTimeout(
					config.connectionAcquisitionTimeoutMillis(),
					TimeUnit.MILLISECONDS)
				.withMaxConnectionPoolSize(config.maxConnectionPoolSize())
				.withMaxConnectionLifetime(
					config.maxConnectionLifetimeMillis(),
					TimeUnit.MILLISECONDS)
				.withRoutingFailureLimit(config.routingFailureLimit())
				.withRoutingRetryDelay(
					config.routingRetryDelayMillis(),
					TimeUnit.MILLISECONDS)
				.withRoutingTablePurgeDelay(
					config.routingTablePurgeDelayMillis(),
					TimeUnit.MILLISECONDS)
				.withConnectionAcquisitionTimeout(
					config.connectionTimeoutMillis(),
					TimeUnit.MILLISECONDS)
				.withFetchSize(config.fetchSize())
				.withMaxTransactionRetryTime(
					config.maxTransactionRetryTime(),
					TimeUnit.MILLISECONDS);

		if (config.idleTimeBeforeConnectionTest() != -1) {
			builder.withConnectionLivenessCheckTimeout(
				config.idleTimeBeforeConnectionTest(),
				TimeUnit.MILLISECONDS);
		}

		if (config.isMetricsEnabled()) {
			builder.withDriverMetrics();
		}

		if (config.logLeakedSessions()) {
			builder.withLeakedSessionsLogging();
		}

		if (config.eventLoopThreads() < 1) {
			builder.withEventLoopThreads(
				2 * Runtime.getRuntime().availableProcessors());
		}

		_driver = GraphDatabase.driver(
			config.uri(),
			AuthTokens.basic(
				config.username(),
				config.password()
			),
			builder.build()
		);
	}

	@Modified
	void modified(Config config) {
		deactivate();
		activate(config);
	}

	@Deactivate
	void deactivate() {
		_driver.close();
		_driver = null;
	}

	public Driver getDriver() {
		return _driver;
	}

	private Driver _driver;

	public static void main(String[] args) throws Exception {
		Driver driver = GraphDatabase.driver(
			"bolt://localhost:7687",
			AuthTokens.basic(
				"neo4j",
				"s3cr3t"
			)
		);

		String[] names = new String[] {"name1", "name2", "name3"};

		Publisher<Record> recordPublisher = driver.rxSession().writeTransaction(
			tx ->
				Flux
					.fromArray(names)
					.flatMap(name ->
						tx.run(
							"CREATE (a:person {entityName: $entityName}) RETURN a;",
							Map.of("entityName", name)
						).records())

		);

		Flux.from(recordPublisher)
			.doOnNext(record -> System.out.println(record))
			.map(Record::asMap)
			.subscribe(System.out::println);

		Thread.sleep(10_000);

	}

}
