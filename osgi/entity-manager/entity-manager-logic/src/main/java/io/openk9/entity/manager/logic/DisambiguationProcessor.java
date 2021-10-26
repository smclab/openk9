package io.openk9.entity.manager.logic;

import io.openk9.entity.manager.model.payload.EntityRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component(
	immediate = true,
	service = DisambiguationProcessor.class
)
public class DisambiguationProcessor {

	@interface Config {
		long amount() default 30_000;
		ChronoUnit unit() default ChronoUnit.MILLIS;
		int concurrency() default Integer.MAX_VALUE;
	}

	private Mono<Void> _disambiguate(
		GroupedFlux<EntityRequest, StartDisambiguation.InternalDisambiguation> group) {

		return group
			.concatMap(_getOrAddEntities::handleMessage)
			.timeout(
				_duration,
				Mono.error(
					new TimeoutException(
						"timeout on key: " + group.key() +
						" (Did not observe any item or terminal signal within " + _duration.toMillis() + "ms)")
				)
			)
			.then();
	}

	@Activate
	void activate(Config config) {

		_duration = Duration.of(config.amount(), config.unit());

		Flux<StartDisambiguation.InternalDisambiguation> flux =
			_startDisambiguation.flux();

		_disposable =
			flux
				.groupBy(
					internalDisambiguation ->
						internalDisambiguation.getRequest().getCurrent(),
					config.concurrency())
				.flatMap(this::_disambiguate, config.concurrency(), config.concurrency())
				.onErrorContinue(TimeoutException.class, (throwable, o) -> {

					if (_log.isDebugEnabled()) {
						_log.debug(throwable.getMessage());
					}

				})
				.subscribe();

	}

	@Modified
	void modified(Config config) {
		deactivate();
		activate(config);
	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
	}

	private Disposable _disposable;

	private Duration _duration;

	@Reference
	private StartDisambiguation _startDisambiguation;

	@Reference
	private GetOrAddEntities _getOrAddEntities;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor(staticName = "of")
	@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
	public static class InternalDisambiguationProcessor
		extends StartDisambiguation.InternalDisambiguation {

		@EqualsAndHashCode.Include
		private EntityRequest current;

		private List<EntityRequest> rest;

		@Delegate
		private StartDisambiguation.InternalDisambiguation source;
	}

	private static final Logger _log = LoggerFactory.getLogger(
		DisambiguationProcessor.class);

}
