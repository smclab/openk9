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

import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;

/**
 * This helper class is used to hold a reference to the
 * instance of the {@link EventBus} object, created by Quarkus
 * during the bootstrap of the application.
 * It is useful when we need to call the eventBus from a class
 * that isn't a CDI bean.
 */
public class EventBusInstanceHolder {

	private static EventBus eventBus;

	public static EventBus getEventBus() {
		return eventBus;
	}

	public static void setEventBus(EventBus eventBus) {
		EventBusInstanceHolder.eventBus = eventBus;
	}

	public static void send(String address, Object message) {
		eventBus.send(address, message);
	}

	public static <T> Uni<Message<T>> request(String address, Object message) {
		return eventBus.request(address, message);
	}

	/**
	 * Sends a request on the event bus, waiting for the reply no longer than
	 * the given timeout.
	 * <p>
	 * The two-arguments overload leaves the reply timeout to the Vert.x
	 * default of 30 seconds, which is too short for a handler doing long
	 * running work, such as splitting and embedding the text of a document:
	 * the caller would fail with {@code ReplyFailure.TIMEOUT} while the
	 * handler is still working.
	 *
	 * @param address the event bus address of the request
	 * @param message the request body
	 * @param timeout how long to wait for the reply before failing
	 * @return a {@link Uni} with the reply, failing with a
	 * {@code ReplyException} of type {@code ReplyFailure.TIMEOUT} when the
	 * handler does not reply in time
	 */
	public static <T> Uni<Message<T>> request(
		String address, Object message, Duration timeout) {

		return eventBus.request(
			address,
			message,
			new DeliveryOptions().setSendTimeout(timeout.toMillis())
		);
	}

	private EventBusInstanceHolder() {}

}
