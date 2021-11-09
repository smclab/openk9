package io.openk9.entity.manager.util;

import org.jboss.logging.Logger;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;

@ApplicationScoped
public class LoggerAggregator {

	@PostConstruct
	public void init() {

		_loggerValueMany =
			Sinks
				.unsafe()
				.many()
				.unicast()
				.onBackpressureBuffer();

		_disposable = _loggerValueMany
			.asFlux()
			.groupBy(LoggerValue::getLoggerKey)
			.flatMap(group ->
				group
					.map(LoggerValue::getLoggerValue)
					.bufferTimeout(1_000, Duration.ofMillis(2_000))
					.map(list -> Tuples.of(group.key(), list))
			)
			.subscribeOn(Schedulers.single())
			.subscribe(t2 -> _logger.info(
				"[AGGREGATION] key: " + t2.getT1() + " values: " + t2.getT2()));
	}

	@PreDestroy
	public void destroy() {
		_disposable.dispose();
		_loggerValueMany.tryEmitComplete();
	}

	public void emitLog(LoggerValue loggerValue) {
		_loggerValueMany.tryEmitNext(loggerValue);
	}

	public void emitLog(String loggerKey, String message) {
		_loggerValueMany.tryEmitNext(LoggerValue.of(loggerKey, message));
	}

	private Sinks.Many<LoggerValue> _loggerValueMany;

	private Disposable _disposable;

	@Inject
	Logger _logger;

}
