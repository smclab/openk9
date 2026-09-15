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

package io.openk9.quarkus.common;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.mutiny.core.Vertx;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The reply timeout of the {@code request} helpers, on a real event bus and a
 * handler that is deliberately slower than a short timeout: it stands in for a
 * long running handler, such as the one splitting and embedding the text of a
 * document. No Quarkus container is booted, the production helper is wired to
 * the bus exactly as the application bootstrap does.
 */
class EventBusInstanceHolderTest {

	private static final String SLOW_ADDRESS = "test#slowHandler";

	private static final String REPLY = "reply";

	/** Longer than {@link #SHORT_TIMEOUT}, shorter than the generous ones. */
	private static final long HANDLER_DELAY_MILLIS = 2_000;

	private static final Duration SHORT_TIMEOUT = Duration.ofMillis(200);

	private static final Duration GENEROUS_TIMEOUT = Duration.ofSeconds(10);

	/** Upper bound of the awaits: it must never be the assertion that fires. */
	private static final Duration AWAIT_AT_MOST = Duration.ofSeconds(30);

	private static Vertx vertx;

	@BeforeAll
	static void setUp() throws InterruptedException {
		vertx = Vertx.vertx();

		// a handler replying only after HANDLER_DELAY_MILLIS
		var registered = new CountDownLatch(1);
		vertx.eventBus().<String>consumer(
				SLOW_ADDRESS,
				message -> vertx.setTimer(
					HANDLER_DELAY_MILLIS, id -> message.reply(REPLY))
			)
			.completionHandler()
			.subscribe().with(unused -> registered.countDown());

		Assertions.assertTrue(
			registered.await(5, TimeUnit.SECONDS),
			"the slow handler was not registered");

		EventBusInstanceHolder.setEventBus(vertx.eventBus());
	}

	@AfterAll
	static void tearDown() {
		EventBusInstanceHolder.setEventBus(null);
		vertx.closeAndAwait();
	}

	@Test
	void request_with_a_timeout_shorter_than_the_handler_fails_with_TIMEOUT() {
		// the production symptom: the caller gives up while the handler works
		var request = EventBusInstanceHolder
			.request(SLOW_ADDRESS, "message", SHORT_TIMEOUT);

		var failure = Assertions.assertThrows(
			ReplyException.class,
			() -> request.await().atMost(AWAIT_AT_MOST));

		Assertions.assertEquals(ReplyFailure.TIMEOUT, failure.failureType());
	}

	@Test
	void request_with_a_generous_timeout_waits_for_the_slow_handler() {
		// the fix: a handler slower than the short budget still completes,
		// because the timeout is the one passed in, not the Vert.x default
		var reply = EventBusInstanceHolder
			.<String>request(SLOW_ADDRESS, "message", GENEROUS_TIMEOUT)
			.await().atMost(AWAIT_AT_MOST);

		Assertions.assertEquals(REPLY, reply.body());
	}

	@Test
	void request_without_a_timeout_keeps_the_default_one() {
		// the two-arguments overload is untouched: the same slow handler that
		// blew the short budget replies within the Vert.x default of 30s, so
		// the overload does not shorten the window of its existing callers
		var reply = EventBusInstanceHolder
			.<String>request(SLOW_ADDRESS, "message")
			.await().atMost(AWAIT_AT_MOST);

		Assertions.assertEquals(REPLY, reply.body());
	}

}
