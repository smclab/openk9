/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
