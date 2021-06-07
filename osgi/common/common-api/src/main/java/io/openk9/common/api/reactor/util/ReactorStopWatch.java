package io.openk9.common.api.reactor.util;

import org.apache.commons.lang3.time.StopWatch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public final class ReactorStopWatch {

	public static <T> Mono<T> stopWatch(
		Mono<T> request, String message, Consumer<String> logger) {

		return Mono.defer(() -> {

			StopWatch stopWatch = _createStarted(message);

			return request
				.doFinally(ignore -> logger.accept(stopWatch.toString()));
		});

	}

	public static <T> Flux<T> stopWatch(
		Flux<T> request, String message, Consumer<String> logger) {

		return Flux.defer(() -> {

			StopWatch stopWatch = _createStarted(message);

			return request
				.doFinally(ignore -> logger.accept(stopWatch.toString()));

		});

	}

	private static StopWatch _createStarted(String message) {
		StopWatch stopWatch = new StopWatch(message);
		stopWatch.start();
		return stopWatch;
	}

}
