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

import io.smallrye.mutiny.Uni;
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

	private EventBusInstanceHolder() {}

}
